package task1;

import org.junit.jupiter.api.Assertions;
import org.tinylog.Logger;
import task1.specification.Broker;
import task1.specification.Channel;
import task1.specification.Task;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Receiver extends Thread {

    private Broker broker;
    private Channel channel;
    private int numberOfMessagesReceived;

    public Receiver() {
        this.numberOfMessagesReceived = 0;
    }

    private Broker getBroker() {
        if (broker == null) {
            broker = Task.getBroker();
        }
        return this.broker;
    }

    private int getPort() {
        return TestRunner.RECEIVING_PORT;
    }

    public boolean establishConnection() {
        try {
            this.channel = getBroker().accept(this.getPort());
        } catch (IOException e) {
            Logger.error("Failed to establish connection in Receiver, error: " + e.getMessage());
            return false;
        }
        return true;
    }

    private int byteArrayToInt(byte[] byteArray) {
        if (byteArray == null || byteArray.length != 4) {
            throw new IllegalArgumentException("Invalid byte array size. Expected 4 bytes.");
        }
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        return buffer.getInt();
    }

    private void receiveMessage() {
        try {
            byte[] lengthBuffer = new byte[4];
            int totalBytesRead = 0;

            while (totalBytesRead < lengthBuffer.length) {
                int bytesRead = channel.read(lengthBuffer, totalBytesRead, lengthBuffer.length - totalBytesRead);
                if (bytesRead == -1) {
                    Logger.error("Failed to read message length in Receiver");
                    return;
                }
                totalBytesRead += bytesRead;
            }

            int messageLength = byteArrayToInt(lengthBuffer);

            byte[] messageBuffer = new byte[messageLength];
            totalBytesRead = 0;

            while (totalBytesRead < messageBuffer.length) {
                int bytesRead = channel.read(messageBuffer, totalBytesRead, messageBuffer.length - totalBytesRead);
                if (bytesRead == -1) {
                    Logger.error("Failed to read message in Receiver");
                    return;
                }
                totalBytesRead += bytesRead;
            }

            System.out.println("<-- Received message in Receiver (" + this.numberOfMessagesReceived + "): " + new String(messageBuffer));

            echoMessageBack(channel, messageBuffer);
        } catch (Exception e) {
            Logger.error("Error receiving message in Receiver: " + e.getMessage());
        }
    }

    private byte[] intToByteArray(int length) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(length);
        return buffer.array();
    }

    private void echoMessageBack(Channel channel, byte[] messageBuffer) {
        byte[] lengthBytes = intToByteArray(messageBuffer.length);
        int totalSent = 0;

        while (totalSent < lengthBytes.length) {
            int sentData = channel.write(lengthBytes, totalSent, lengthBytes.length - totalSent);
            totalSent += sentData;
        }

        totalSent = 0;
        while (totalSent < messageBuffer.length) {
            int sentData = channel.write(messageBuffer, totalSent, messageBuffer.length - totalSent);
            totalSent += sentData;
        }
    }

    private void testDisconnectHandling() {
        Assertions.assertThrows(Exception.class, () -> {
            System.out.println("Testing disconnection...");
            this.channel.disconnect();
            byte[] testBuffer = new byte[10];
            this.channel.read(testBuffer, 0, testBuffer.length); // Should throw exception
        });
    }

    private void receiveMessages() {
        int numberOfMessages = TestRunner.NUMBER_OF_MESSAGES;
        while (numberOfMessages > 0) {
            receiveMessage();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("Failed to sleep in Receiver");
            }
            numberOfMessages--;
            this.numberOfMessagesReceived++;
        }
        testDisconnectHandling();  // Run disconnection test after receiving messages
    }

    public void disconnect() {
        if (channel != null) {
            channel.disconnect();
        }
    }



    @Override
    public void run() {
        boolean connected = establishConnection();
        if (!connected) {
            throw new IllegalStateException("Failed to establish connection in Receiver, check Duplicate Connection");
        }
        receiveMessages();
    }
}
