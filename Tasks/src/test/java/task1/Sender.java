package task1;

import task1.specification.Broker;
import task1.specification.Channel;
import task1.specification.MessageQueue;
import task1.specification.Task;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Sender extends Thread {

    private String message;
    private String receiverBrokerName;
    private Broker broker;
    private Channel channel;

    public Sender(String message, String receiverBrokerName) {
        this.message = message;
        this.receiverBrokerName = receiverBrokerName;
    }

    private Broker getBroker() {
        if (broker == null) {
            this.broker = Task.getBroker();
        }
        return this.broker;
    }

    private int getPort() {
        return TestRunner.SENDING_PORT;
    }

    public boolean establishConnection() {
        try {
            this.channel = getBroker().connect(receiverBrokerName, getPort());
            if (channel == null) {
                System.out.println("Failed to establish connection in Sender");
                return false;
            }
        } catch (IOException e) {
            System.out.println("Failed to establish connection in Sender");
            return false;
        }
        return true;
    }

    private void sendMessage() {
        byte[] messageBytesArray = message.getBytes();
        byte[] lengthBytes = intToByteArray(messageBytesArray.length);
        int totalSent = 0;

        while (totalSent < lengthBytes.length) {
            int sentData = channel.write(lengthBytes, totalSent, lengthBytes.length - totalSent);
            if (sentData == -1) {
                System.out.println("Failed to send message length in Sender");
                return;
            }
            totalSent += sentData;
        }

        totalSent = 0;
        while (totalSent < messageBytesArray.length) {
            int sentData = channel.write(messageBytesArray, totalSent, messageBytesArray.length - totalSent);
            if (sentData == -1) {
                System.out.println("Failed to send message in Sender");
                return;
            }
            totalSent += sentData;
        }
    }

    private byte[] intToByteArray(int length) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(length);
        return buffer.array();
    }

    private void testDisconnectHandling() {
        try {
            System.out.println("Testing disconnection...");
            this.channel.disconnect();
            byte[] testBuffer = new byte[10];
            this.channel.write(testBuffer, 0, testBuffer.length); // Should throw exception
        } catch (Exception e) {
            System.out.println("Disconnection test passed: " + e.getMessage());
        }
    }

    private void infiniteLoopSending() {
        int nbMessages = TestRunner.NUMBER_OF_MESSAGES;

        while (nbMessages > 0) {
            sendMessage();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Failed to sleep in Sender");
            }

            nbMessages--;
        }

        testDisconnectHandling();  // Run disconnection test after sending messages
        disconnect();
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

        infiniteLoopSending();
    }

}
