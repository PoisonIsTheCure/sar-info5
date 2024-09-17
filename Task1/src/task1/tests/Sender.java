package task1.tests;

import task1.specification.Broker;
import task1.specification.Channel;
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
            broker = Task.getBroker();
        }
        return broker;
    }

    private int getPort() {
        return TestRunner.SENDING_PORT;
    }

    public boolean establishConnection() {
        try {
            this.channel = getBroker().connect(receiverBrokerName, getPort());
        } catch (IOException e) {
            System.out.println("Failed to establish connection in Sender");
            return false;
        }
        return true;
    }

    private void sendMessage() {
        byte[] messageBytesArray = message.getBytes();

        // Send message length to the channel
        byte[] lengthBytes = intToByteArray(messageBytesArray.length);
        int totalSent = 0;

        // Ensure the entire length array is sent
        while (totalSent < lengthBytes.length) {
            int sentData = channel.write(lengthBytes, totalSent, lengthBytes.length - totalSent);
            if (sentData == -1) {
                System.out.println("Failed to send message length in Sender");
                return;
            }
            totalSent += sentData;
        }

        // Send the actual message
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
        // Allocate a ByteBuffer , 4 bytes for an integer
        ByteBuffer buffer = ByteBuffer.allocate(4);

        buffer.putInt(length);

        return buffer.array();
    }


    private void infiniteLoopSending() {
        int nbMessages = TestRunner.NUMBER_OF_MESSAGES;

        while(nbMessages > 0) {
            sendMessage();
            // Sleep for 1 second
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Failed to sleep in Sender");
            }

            nbMessages--;
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

        infiniteLoopSending();

    }

}
