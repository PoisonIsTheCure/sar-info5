package task2;

import org.junit.jupiter.api.Assertions;
import org.tinylog.Logger;
import task2.specification.MessageQueue;
import task2.specification.QueueBroker;
import task2.specification.Task;

public class MessageSender extends Thread {

    private String message;
    private String receiverBrokerName;
    private QueueBroker queueBroker;
    private MessageQueue messageQueue;

    private int port = MessageQueueTest.SENDING_PORT;

    public MessageSender(String message, String receiverBrokerName) {
        this.message = message;
        this.receiverBrokerName = receiverBrokerName;
    }

    public MessageSender(String message, String receiverBrokerName, int port) {
        this.message = message;
        this.receiverBrokerName = receiverBrokerName;
        this.port = port;
    }

    private QueueBroker getQueueBroker() {
        if (queueBroker == null) {
            this.queueBroker = Task.getQueueBroker();
        }
        return this.queueBroker;
    }

    private int getPort() {
        return this.port;
    }

    /**
     * Establish connection by creating a MessageQueue to communicate with the receiver.
     */
    public boolean establishConnection() {
        try {
            // Use the broker to establish the message queue connection
            this.messageQueue = getQueueBroker().connect(receiverBrokerName, getPort());
            if (messageQueue == null) {
                Logger.error("Failed to establish connection in MessageSender");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Failed to establish connection in MessageSender: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Sends the message through the MessageQueue.
     */
    private void sendMessage() {
        byte[] msg = this.message.getBytes();
        this.messageQueue.send(msg, 0, msg.length);
    }


    /**
     * Disconnects the MessageQueue after finishing sending messages.
     */
    public void disconnect() {
        if (messageQueue != null) {
            messageQueue.close();
        }
    }

    public boolean echoReceive() {
        byte[] messageBuffer = messageQueue.receive();
        if (messageBuffer == null) {
            Logger.error("Failed to receive message in MessageSender");
            return false;
        }

        System.out.println("<-- Received echo in Sender: " + new String(messageBuffer));
        return true;
    }

    /**
     * Simulates sending multiple messages in a loop.
     */
    private void infiniteLoopSending() {
        int nbMessages = MessageQueueTest.NUMBER_OF_MESSAGES;

        while (nbMessages > 0) {
            sendMessage();

            // Receive the echo message from the receiver
            Assertions.assertTrue(echoReceive());
            try {
                Thread.sleep(100); // Simulate a delay between sending messages
            } catch (InterruptedException e) {
                System.out.println("Failed to sleep in MessageSender");
            }
            nbMessages--;
        }

        disconnect();  // Disconnect after all messages have been sent
    }

    @Override
    public void run() {
        boolean connected = establishConnection();
        if (!connected) {
            return;
        }

        infiniteLoopSending();  // Start sending messages in a loop
    }
}