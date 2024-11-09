package task2;

import task2.specification.MessageQueue;
import task2.specification.QueueBroker;
import task2.specification.Task;

public class MessageReceiver extends Thread {

    private QueueBroker queueBroker;
    private MessageQueue messageQueue;
    private int numberOfMessagesReceived;

    public MessageReceiver() {
        this.numberOfMessagesReceived = 0;
    }

    private QueueBroker getQueueBroker() {
        if (queueBroker == null) {
            queueBroker = Task.getQueueBroker();
        }
        return this.queueBroker;
    }

    private int getPort() {
        return TestRunner.RECEIVING_PORT;
    }

    /**
     * Establish connection by creating a MessageQueue to communicate with the sender.
     */
    public boolean establishConnection() {
        try {
            // Use the broker to accept the message queue connection
            this.messageQueue = getQueueBroker().accept(getPort());
            if (messageQueue == null) {
                System.out.println("Failed to establish connection in MessageReceiver");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Failed to establish connection in MessageReceiver: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Receives a message from the MessageQueue.
     */
    private void receiveMessage() {
        try {
            // Read the actual message
            byte[] messageBuffer = messageQueue.receive();
            if (messageBuffer == null) {
                System.out.println("Failed to receive message in MessageReceiver");
                return;
            }

            System.out.println("Received message (" + this.numberOfMessagesReceived + "): " + new String(messageBuffer));

        } catch (Exception e) {
            System.out.println("Error receiving message in MessageReceiver: " + e.getMessage());
        }
    }

    /**
     * Helper method to convert a byte array to an integer.
     */
    private int byteArrayToInt(byte[] byteArray) {
        if (byteArray == null || byteArray.length != 4) {
            throw new IllegalArgumentException("Invalid byte array size. Expected 4 bytes.");
        }
        return java.nio.ByteBuffer.wrap(byteArray).getInt();
    }

    /**
     * Disconnects the MessageQueue after finishing receiving messages.
     */
    public void disconnect() {
        if (messageQueue != null) {
            messageQueue.close();
        }
    }

    /**
     * Simulates receiving multiple messages in a loop.
     */
    private void infiniteLoopReceiving() {
        int numberOfMessages = TestRunner.NUMBER_OF_MESSAGES;
        while (numberOfMessages > 0) {
            receiveMessage();
            try {
                Thread.sleep(1000); // Simulate a delay between receiving messages
            } catch (InterruptedException e) {
                System.out.println("Failed to sleep in MessageReceiver");
            }
            numberOfMessages--;
            this.numberOfMessagesReceived++;
        }

        disconnect();  // Disconnect after all messages have been received
    }

    @Override
    public void run() {
        boolean connected = establishConnection();
        if (!connected) {
            return;
        }

        infiniteLoopReceiving();  // Start receiving messages in a loop
    }
}