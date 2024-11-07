package task1.implementation;

import task1.specification.Channel;
import task1.specification.DisconnectedException;
import task1.specification.MessageQueue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class MessageQueueImpl extends MessageQueue {

    private final Channel channel;
    private final Queue<Message> messagesToSend;
    private final Queue<byte[]> receivedMessages;
    private volatile boolean isClosed;

    private final Semaphore sendSemaphore;
    private final Semaphore receiveSemaphore;

    // Worker threads to handle asynchronous sending and receiving
    private Thread senderWorker;
    private Thread receiverWorker;

    public MessageQueueImpl(Channel channel) {
        this.channel = channel;
        this.messagesToSend = new LinkedList<>();
        this.receivedMessages = new LinkedList<>();
        this.isClosed = false;

        this.sendSemaphore = new Semaphore(0);  // Starts with 0 permits
        this.receiveSemaphore = new Semaphore(0);  // Starts with 0 permits

        // Start the sender and receiver worker threads
        startSenderWorker();
        startReceiverWorker();
    }

    @Override
    public void send(byte[] bytes, int offset, int length) throws DisconnectedException {
        if (isClosed) {
            throw new DisconnectedException("MessageQueue is closed. Cannot send messages.");
        }

        Message message = new Message(bytes, offset, length);
        synchronized (messagesToSend) {
            messagesToSend.offer(message);
        }

        // Release a permit to indicate that a message is ready to be sent
        sendSemaphore.release();
    }

    @Override
    public byte[] receive() throws DisconnectedException {
        if (isClosed) {
            throw new DisconnectedException("MessageQueue is closed. Cannot receive messages.");
        }

        try {
            // Acquire a permit to ensure a message is available before proceeding
            receiveSemaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (isClosed) {
                return null;  // Exit if interrupted due to closure
            }
        }

        synchronized (receivedMessages) {
            return receivedMessages.poll();  // Retrieve the message
        }
    }

    @Override
    public void close() {
        isClosed = true;
        channel.disconnect();  // Disconnect the channel

        // Interrupt and join the worker threads
        if (senderWorker != null) {
            senderWorker.interrupt();
            try {
                senderWorker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();  // Restore the interrupted status
            }
        }

        if (receiverWorker != null) {
            receiverWorker.interrupt();
            try {
                receiverWorker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();  // Restore the interrupted status
            }
        }
    }

    @Override
    public boolean closed() {
        return isClosed && channel.disconnected();
    }

    // Worker thread that processes the message queue and sends messages asynchronously
    private void startSenderWorker() {
        senderWorker = new Thread(() -> {
            while (!isClosed) {
                try {
                    // Acquire a permit, wait if no messages are available to send
                    sendSemaphore.acquire();
                } catch (InterruptedException e) {
                    if (isClosed) {
                        return;  // Exit if interrupted due to closure
                    }
                    Thread.currentThread().interrupt();  // Restore interrupted status
                }

                Message messageToSend;
                synchronized (messagesToSend) {
                    messageToSend = messagesToSend.poll();
                }

                if (messageToSend != null) {
                    sendMessageToChannel(messageToSend);
                }
            }
        });

        senderWorker.start();  // Start the sender worker thread
    }

    // Worker thread that reads messages from the channel and adds them to the receivedMessages queue
    private void startReceiverWorker() {
        receiverWorker = new Thread(() -> {
            while (!isClosed) {
                try {
                    byte[] lengthBuffer = new byte[4];
                    int totalBytesRead = 0;

                    // Read the message length (first 4 bytes)
                    while (totalBytesRead < lengthBuffer.length) {
                        int bytesRead = channel.read(lengthBuffer, totalBytesRead, lengthBuffer.length - totalBytesRead);
                        if (bytesRead == -1) {
                            throw new DisconnectedException("Failed to read message length, channel disconnected.");
                        }
                        totalBytesRead += bytesRead;
                    }

                    // Convert the byte array to an integer for message length
                    int messageLength = byteArrayToInt(lengthBuffer);

                    // Read the actual message
                    byte[] messageBuffer = new byte[messageLength];
                    totalBytesRead = 0;
                    while (totalBytesRead < messageBuffer.length) {
                        int bytesRead = channel.read(messageBuffer, totalBytesRead, messageBuffer.length - totalBytesRead);
                        if (bytesRead == -1) {
                            throw new DisconnectedException("Failed to read message, channel disconnected.");
                        }
                        totalBytesRead += bytesRead;
                    }

                    // Add the received message to the queue
                    synchronized (receivedMessages) {
                        receivedMessages.offer(messageBuffer);
                    }

                    // Release a permit to indicate that a new message has been received
                    receiveSemaphore.release();

                } catch (DisconnectedException e) {
                    if (isClosed) {
                        System.out.println("Channel disconnected : " + e.getMessage());
                        return;  // Exit the thread if the queue is closed
                    }

                    // TODO: Check if the disconnection scenario is solid
                    this.close();
                    System.out.println("Channel disconnected : " + e.getMessage());
                }
            }
        });

        receiverWorker.start();  // Start the receiver worker thread
    }

    // Helper method to send a message over the channel
    private void sendMessageToChannel(Message message) {
        byte[] bytes = message.getMessage();
        int offset = message.getOffset();
        int length = message.getLength();
        int totalBytesSent = 0;

        try {
            // Send the message length (first 4 bytes)
            byte[] lengthBytes = intToByteArray(length);
            while (totalBytesSent < lengthBytes.length) {
                int bytesSent = channel.write(lengthBytes, totalBytesSent, lengthBytes.length - totalBytesSent);
                if (bytesSent == -1) {
                    throw new DisconnectedException("Failed to send message length, channel disconnected.");
                }
                totalBytesSent += bytesSent;
            }

            // Send the actual message
            totalBytesSent = 0;

            while (totalBytesSent < length) {
                int bytesSent = channel.write(bytes, offset + totalBytesSent, length - totalBytesSent);
                if (bytesSent == -1) {
                    throw new DisconnectedException("Failed to send message, channel disconnected.");
                }
                totalBytesSent += bytesSent;
            }
        } catch (DisconnectedException e) {
            System.out.println("Channel disconnected while sending message: " + e.getMessage());
        }
    }

    private byte[] intToByteArray(int length) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(length);
        return buffer.array();
    }

    private int byteArrayToInt(byte[] byteArray) {
        if (byteArray == null || byteArray.length != 4) {
            throw new IllegalArgumentException("Invalid byte array size. Expected 4 bytes.");
        }
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        return buffer.getInt();
    }
}