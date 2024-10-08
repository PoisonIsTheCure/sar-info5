package task3.tests;

import task3.specification.MessageQueue;
import task3.specification.QueueBroker;
import task3.specification.Task;

public class MessageReceiver extends Thread {

    private QueueBroker queueBroker;
    private MessageQueue messageQueue;
    private int numberOfMessagesReceived;
    private boolean bindRequestAccepted;

    public MessageReceiver() {
        this.numberOfMessagesReceived = 0;
        this.bindRequestAccepted = false;
    }

    private QueueBroker getQueueBroker() {
        if (queueBroker == null) {
            queueBroker = Task.getQueueBroker();
        }
        return this.queueBroker;
    }

    private int getPort() {
        return MessageQueueTest.RECEIVING_PORT;
    }

    /**
     * Establish connection by creating a MessageQueue to communicate with the sender.
     */
    public void establishConnection() {
        getQueueBroker().bind(getPort(), new QueueBroker.AcceptListener() {
            @Override
            public void accepted(MessageQueue messageQueue) {
                MessageReceiver.this.bindRequestAccepted = true;
                MessageReceiver.this.messageQueue = messageQueue;
            }
        });
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
            this.queueBroker.unbind(getPort());
            messageQueue.close();
        }
    }

    /**
     * Simulates receiving multiple messages in a loop.
     */
    private void infiniteLoopReceiving() {
        int numberOfMessages = MessageQueueTest.NUMBER_OF_MESSAGES;
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
        establishConnection();
        if (!this.bindRequestAccepted) {
            try {
                Thread.sleep(1000); // Simulate a delay before retrying to connect
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        infiniteLoopReceiving();  // Start receiving messages in a loop
    }
}