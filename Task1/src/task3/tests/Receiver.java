package task3.tests;

import task3.specification.Broker;
import task3.specification.Channel;
import task3.specification.Task;

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
            System.out.println("Failed to establish connection in Receiver");
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
                    System.out.println("Failed to read message length in Receiver");
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
                    System.out.println("Failed to read message in Receiver");
                    return;
                }
                totalBytesRead += bytesRead;
            }

            System.out.println("Received message (" + this.numberOfMessagesReceived + "): " + new String(messageBuffer));

        } catch (Exception e) {
            System.out.println("Error receiving message in Receiver: " + e.getMessage());
        }
    }

    private void testDisconnectHandling() {
        try {
            System.out.println("Testing disconnection...");
            this.channel.disconnect();
            byte[] testBuffer = new byte[10];
            this.channel.read(testBuffer, 0, testBuffer.length); // Should throw exception
        } catch (Exception e) {
            System.out.println("Disconnection test passed: " + e.getMessage());
        }
    }

    private void infiniteLoopReceiving() {
        int numberOfMessages = TestRunner.NUMBER_OF_MESSAGES;
        while (numberOfMessages > 0) {
            receiveMessage();
            try {
                Thread.sleep(1000);
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
            return;
        }
        infiniteLoopReceiving();
    }
}
