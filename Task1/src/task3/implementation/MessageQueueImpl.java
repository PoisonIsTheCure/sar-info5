package task3.implementation;

import task3.specification.Channel;
import task3.specification.DisconnectedException;
import task3.specification.EventPump;
import task3.specification.MessageQueue;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

import java.util.concurrent.Semaphore;

public class MessageQueueImpl extends MessageQueue {

    private final Channel channel;
    private final Queue<Message> messagesToSend;
    private final Queue<byte[]> receivedMessages;
    private volatile boolean isClosed;
    private EventPump eventPump;

    private enum MessageReceiverState {
        IDLE,
        RECEIVING_LENGTH,
        RECEIVING_MESSAGE
    }

    private enum MessageSenderState {
        IDLE,
        SENDING_LENGTH,
        SENDING_MESSAGE
    }

    private MessageReceiverState receiverState;
    private MessageSenderState senderState;

    // messages length
    private int lengthMessageToSend;
    private int lengthMessageToReceive;



    // Worker threads to handle asynchronous sending and receiving
    private Thread senderWorker;
    private Thread receiverWorker;

    public MessageQueueImpl(Channel channel) {
        this.channel = channel;
        this.messagesToSend = new LinkedList<>();
        this.receivedMessages = new LinkedList<>();
        this.isClosed = false;
        this.eventPump = EventPump.getInstance();
        this.receiverState = MessageReceiverState.IDLE;
        this.senderState = MessageSenderState.IDLE;
    }

    @Override
    public void setListener(MessageQueue.Listener l) {

    }

    @Override
    public void send(byte[] bytes, int offset, int length) throws DisconnectedException {
        if (isClosed) {
            throw new DisconnectedException("MessageQueue is closed. Cannot send messages.");
        }

        this.eventPump.post(new MessageSendEvent(bytes, offset, length, channel));
    }

    @Override
    public void send(byte[] bytes) throws DisconnectedException {
        send(bytes, 0, bytes.length);
    }

    private void receiveMessageIteration() throws DisconnectedException {
        byte[] message = null;
        switch (receiverState) {
            case IDLE:
                receiverState = MessageReceiverState.RECEIVING_LENGTH;
                this.eventPump.post(new MessageReceiveEvent(channel, 4, new MessageReceiveEvent.InternalListener() {
                    @Override
                    public void received(byte[] message) {
                        MessageQueueImpl.this.lengthMessageToReceive = byteArrayToInt(message);
                        MessageQueueImpl.this.receiverState = MessageReceiverState.RECEIVING_MESSAGE;
                    }
                }));
                break;
            case RECEIVING_LENGTH:
                break;
            case RECEIVING_MESSAGE:
                message = new byte[lengthMessageToReceive];

                this.eventPump.post(new MessageReceiveEvent(channel, lengthMessageToReceive, new MessageReceiveEvent.InternalListener() {
                    @Override
                    public void received(byte[] message) {
                        receivedMessages.add(message);
                        MessageQueueImpl.this.receiverState = MessageReceiverState.IDLE;
                        MessageQueueImpl.this.listener.received(message);
                    }
                }));
                receiverState = MessageReceiverState.IDLE;
                break;
        }
    }

    @Override
    public void receive() throws DisconnectedException {
        if (isClosed) {
            throw new DisconnectedException("MessageQueue is closed. Cannot receive messages.");
        }
        receiveMessageIteration(); // Receive the message length
        receiveMessageIteration(); // Receive the message, listener will be notified directly
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