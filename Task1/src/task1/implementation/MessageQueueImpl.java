package task1.implementation;

import task1.specification.Channel;
import task1.specification.DisconnectedException;
import task1.specification.MessageQueue;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class MessageQueueImpl extends MessageQueue {

    private final Channel channel;
    private final Queue<Message> messagesToSend;
    private final Queue<byte[]> receivedMessages;
    private volatile boolean isClosed;
    private final Object sendLock = new Object();
    private final Object receiveLock = new Object();

    // Worker threads to handle asynchronous sending and receiving
    private Thread senderWorker;
    private Thread receiverWorker;

    public MessageQueueImpl(Channel channel) {
        this.channel = channel;
        this.messagesToSend = new LinkedList<>();
        this.receivedMessages = new LinkedList<>();
        this.isClosed = false;

        // Start the sender and receiver worker threads
        startSenderWorker();
        startReceiverWorker();
    }

    @Override
    public synchronized void send(byte[] bytes, int offset, int length) throws DisconnectedException {
        if (isClosed) {
            throw new DisconnectedException("MessageQueue is closed. Cannot send messages.");
        }

        // Create a new message and add it to the queue
        Message message = new Message(bytes, offset, length);
        synchronized (sendLock) {
            messagesToSend.offer(message);
            sendLock.notify();  // Notify the sender worker thread that there's a new message
        }
    }

    @Override
    public synchronized byte[] receive() throws DisconnectedException {
        if (isClosed) {
            throw new DisconnectedException("MessageQueue is closed. Cannot receive messages.");
        }

        // Block if no messages are available in the queue
        synchronized (receiveLock) {
            while (receivedMessages.isEmpty()) {
                try {
                    receiveLock.wait();  // Wait until a new message is received
                } catch (InterruptedException e) {
                    if (isClosed) {
                        return null;  // Exit if interrupted due to closure
                    }
                    Thread.currentThread().interrupt();  // Restore the interrupted status
                }
            }
            byte[] nextMessage;
            synchronized (receivedMessages){
                nextMessage = receivedMessages.poll();
            }
            return nextMessage;  // Return the next message from the queue
        }
    }

    @Override
    public synchronized void close() {
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
                Message messageToSend;

                synchronized (sendLock) {
                    while (messagesToSend.isEmpty()) {
                        try {
                            sendLock.wait();  // Wait for new messages to be added to the queue
                        } catch (InterruptedException e) {
                            if (isClosed) {
                                return;  // Exit if interrupted due to closure
                            }
                            Thread.currentThread().interrupt();  // Restore interrupted status
                        }
                    }

                    synchronized (messagesToSend){
                        // Retrieve the message from the queue
                        messageToSend = messagesToSend.poll();
                    }
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
                    synchronized (receiveLock) {
                        synchronized (receivedMessages){
                            receivedMessages.offer(messageBuffer);
                        }

                        receiveLock.notify();  // Notify waiting threads that a new message is available
                    }

                } catch (DisconnectedException e) {
                    if (isClosed) {
                        return;  // Exit the thread if the queue is closed
                    }
                    System.out.println("Error receiving message: " + e.getMessage());
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

        try {
            int totalBytesSent = 0;
            while (totalBytesSent < length) {
                int bytesSent = channel.write(bytes, offset + totalBytesSent, length - totalBytesSent);
                if (bytesSent == -1) {
                    throw new DisconnectedException("Failed to send message, channel disconnected.");
                }
                totalBytesSent += bytesSent;
            }
        } catch (DisconnectedException e) {
            System.out.println("Error sending message: " + e.getMessage());
        }
    }

    // Helper method to convert a byte array to an integer
    private int byteArrayToInt(byte[] byteArray) {
        if (byteArray == null || byteArray.length != 4) {
            throw new IllegalArgumentException("Invalid byte array size. Expected 4 bytes.");
        }
        return java.nio.ByteBuffer.wrap(byteArray).getInt();
    }
}