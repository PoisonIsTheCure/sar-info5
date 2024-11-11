package task1;

import org.junit.jupiter.api.Assertions;
import org.tinylog.Logger;
import task1.specification.Broker;
import task1.specification.Channel;
import task1.specification.Task;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Sender extends Thread {

    private final String message;
    private final String receiverBrokerName;
    private Broker broker;
    private Channel channel;
    private int port = TestRunner.SENDING_PORT;
    private int echoMessageReceived = 0;

    public Sender(String message, String receiverBrokerName) {
        this.message = message;
        this.receiverBrokerName = receiverBrokerName;
    }

    public Sender(String message, String receiverBrokerName, int port) {
        this.message = message;
        this.receiverBrokerName = receiverBrokerName;
        this.port = port;
    }

    private Broker getBroker() {
        if (broker == null) {
            this.broker = Task.getBroker();
        }
        return this.broker;
    }

    private int getPort() {
        return this.port;
    }

    public boolean establishConnection() {
        try {
            this.channel = getBroker().connect(receiverBrokerName, getPort());
            if (channel == null) {
                Logger.error("Failed to establish connection in Sender");
                return false;
            }
        } catch (IOException e) {
            Logger.error("Failed to establish connection in Sender");
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
                Logger.error("Failed to send message length in Sender");
                return;
            }
            totalSent += sentData;
        }

        totalSent = 0;
        while (totalSent < messageBytesArray.length) {
            int sentData = channel.write(messageBytesArray, totalSent, messageBytesArray.length - totalSent);
            if (sentData == -1) {
                Logger.error("Failed to send message in Sender");
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
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Logger.error("Failed to sleep in Sender");
            }

            nbMessages--;
            Assertions.assertTrue(echoReception());
        }

        testDisconnectHandling();  // Run disconnection test after sending messages
        disconnect();
    }

    private int byteArrayToInt(byte[] byteArray) {
        if (byteArray == null || byteArray.length != 4) {
            throw new IllegalArgumentException("Invalid byte array size. Expected 4 bytes.");
        }
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        return buffer.getInt();
    }

    public boolean echoReception() {
        byte[] lengthBuffer = new byte[4];
        int totalBytesRead = 0;

        while (totalBytesRead < lengthBuffer.length) {
            int bytesRead = channel.read(lengthBuffer, totalBytesRead, lengthBuffer.length - totalBytesRead);
            if (bytesRead == -1) {
                Logger.error("Failed to read message length in Sender");
                return false;
            }
            totalBytesRead += bytesRead;
        }

        int messageLength = byteArrayToInt(lengthBuffer);

        byte[] messageBuffer = new byte[messageLength];
        totalBytesRead = 0;

        while (totalBytesRead < messageBuffer.length) {
            int bytesRead = channel.read(messageBuffer, totalBytesRead, messageBuffer.length - totalBytesRead);
            if (bytesRead == -1) {
                Logger.error("Failed to read message in Sender");
                return false;
            }
            totalBytesRead += bytesRead;
        }

        System.out.println("--> Received echo message ("+ echoMessageReceived +") in Sender '" + getBroker().getName()+ "' : " + new String(messageBuffer));

        echoMessageReceived++;
        return true;
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
