package task1.tests;

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
        return broker;
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
        // Check if the input byte array is valid
        if (byteArray == null || byteArray.length != 4) {
            throw new IllegalArgumentException("Invalid byte array size. Expected 4 bytes.");
        }
        // Wrap the byte array into a ByteBuffer
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        // Retrieve the integer value from the buffer
        return buffer.getInt();
    }


    private void receiveMessage() {
        try {
            // Read message length from the channel
            byte[] lengthBuffer = new byte[4]; // Integer has 4 bytes
            int totalBytesRead = 0;

            // Read exactly 4 bytes to get the message length
            while (totalBytesRead < lengthBuffer.length) {
                int bytesRead = channel.read(lengthBuffer, totalBytesRead, lengthBuffer.length - totalBytesRead);
                if (bytesRead == -1) {
                    System.out.println("Failed to read message length in Receiver");
                    return;
                }
                totalBytesRead += bytesRead;
            }

            int messageLength = byteArrayToInt(lengthBuffer);

            // Read the actual message
            byte[] messageBuffer = new byte[messageLength];
            totalBytesRead = 0;

            // Read messageLength bytes to get the full message
            while (totalBytesRead < messageBuffer.length) {
                int bytesRead = channel.read(messageBuffer, totalBytesRead, messageBuffer.length - totalBytesRead);
                if (bytesRead == -1) {
                    System.out.println("Failed to read message in Receiver");
                    return;
                }
                totalBytesRead += bytesRead;
            }

            // Print the received message
            System.out.println("Received message ("+ this.numberOfMessagesReceived +"): " + new String(messageBuffer));

        } catch (Exception e) {
            System.out.println("Error receiving message in Receiver: " + e.getMessage());
        }
    }


    private void infiniteLoopReceiving() {
        int numberOfMessages = TestRunner.NUMBER_OF_MESSAGES;
        while (numberOfMessages > 0) {
            receiveMessage();
            // Sleep for a short period to avoid busy-waiting
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Failed to sleep in Receiver");
            }

            numberOfMessages--;
            this.numberOfMessagesReceived++;
        }
    }

    public void disconnect() {
        if (channel != null) {
            channel.disconnect();
        }
    }

    public void run() {
        boolean connected = establishConnection();
        if (!connected) {
            return;
        }

        infiniteLoopReceiving();
    }

}
