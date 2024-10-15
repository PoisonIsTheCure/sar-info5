package task3.implementation;

import task3.specification.Channel;
import task3.specification.DisconnectedException;
import task3.specification.ETask;
import task3.specification.MessageQueue;

import java.nio.ByteBuffer;
import java.util.Queue;

public class MQReceiver implements Runnable{
    private Channel channel;
    private MessageQueue parentMessageQueue;

    public MQReceiver(MessageQueue mq, Channel channel) {
        this.parentMessageQueue = mq;
        this.channel = channel;
    }

    @Override
    public void run() {
        while (true) {
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

            Message message = new Message(messageBuffer, 0, messageBuffer.length);
            parentMessageQueue.getListener().received(message.getMessage());
        }
    }

    private int byteArrayToInt(byte[] byteArray) {
        if (byteArray == null || byteArray.length != 4) {
            throw new IllegalArgumentException("Invalid byte array size. Expected 4 bytes.");
        }
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        return buffer.getInt();
    }
}
