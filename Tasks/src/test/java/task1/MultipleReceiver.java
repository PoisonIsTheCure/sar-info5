package task1;

import org.junit.jupiter.api.Assertions;
import org.tinylog.Logger;
import task1.specification.Broker;
import task1.specification.Channel;
import task1.specification.Task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultipleReceiver extends Thread {
    private Broker broker;
    private HashMap<Channel,Integer> channels;
    private int numberOfMessagesReceived;
    public static final int PORT_1 = 5000;
    public static final int PORT_2 = 5001;

    public MultipleReceiver() {
        this.numberOfMessagesReceived = 0;
        this.channels = new HashMap<>();
    }

    private Broker getBroker() {
        if (broker == null) {
            broker = Task.getBroker();
        }
        return this.broker;
    }

    public boolean establishConnection() {
        try {
            Channel channel1 = getBroker().accept(PORT_1);
            Channel channel2 = getBroker().accept(PORT_2);

            this.channels.put(channel1,TestRunner.NUMBER_OF_MESSAGES);
            this.channels.put(channel2,TestRunner.NUMBER_OF_MESSAGES);
        } catch (IOException e) {
            Logger.error("Failed to establish connection in Receiver, error: {}", e.getMessage());
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
        for (Map.Entry<Channel,Integer> entry : this.channels.entrySet()) {
            Channel channel = entry.getKey();
            int numberOfMessages = entry.getValue();
            if (numberOfMessages > 0) {
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

                while (totalBytesRead < messageLength) {
                    int bytesRead = channel.read(messageBuffer, totalBytesRead, messageLength - totalBytesRead);
                    if (bytesRead == -1) {
                        Logger.error("Failed to read message in Receiver");
                        return;
                    }
                    totalBytesRead += bytesRead;
                }

                String message = new String(messageBuffer);
                Logger.info("--> Received message: '{}' from channel {}", message, channel);
                entry.setValue(numberOfMessages-1);

                // Echo the message back
                echoMessageBack(channel, messageBuffer);
            }
        }
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

    private byte[] intToByteArray(int length) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(length);
        return buffer.array();
    }

    private void testDisconnectHandling() {
        for (Map.Entry<Channel,Integer> entry : this.channels.entrySet()) {

            Assertions.assertThrows(Exception.class, () -> {
                System.out.println("Testing disconnection...");
                entry.getKey().disconnect();
                byte[] testBuffer = new byte[10];
                entry.getKey().read(testBuffer, 0, testBuffer.length); // Should throw exception
            });
        }
    }

    private void receiveMessages() {
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
        for (Channel channel : this.channels.keySet()) {
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
