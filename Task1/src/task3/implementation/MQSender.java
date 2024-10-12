package task3.implementation;

import task3.specification.Channel;
import task3.specification.DisconnectedException;
import task3.specification.MessageQueue;

import java.nio.ByteBuffer;
import java.util.Queue;

public class MQSender implements Runnable {
    private Channel channel;
    private Queue<Message> messagesToSend;
    private MessageQueue parentMessageQueue;

    public MQSender(MessageQueue mq,Channel channel) {
        this.parentMessageQueue = mq;
        this.channel = channel;
    }

    public void send(byte[] bytes, int offset, int length) {
        Message message = new Message(bytes, offset, length);
        messagesToSend.add(message);
    }



    @Override
    public void run() {
        while (true){
            if (messagesToSend.isEmpty()) {
                try {
                    Thread.sleep(1000); // TODO: Replace it by a Semaphore maybe ?
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else {
                Message message = messagesToSend.poll();
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
                }
                catch (DisconnectedException e) {
                    System.out.println("Channel disconnected while sending message: " + e.getMessage());
                }
            }
        }
    }

    private byte[] intToByteArray(int length) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(length);
        return buffer.array();
    }
}
