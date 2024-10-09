package task3.tests;

import task3.specification.MessageQueue;
import task3.specification.QueueBroker;
import task3.specification.Task;

public class MessageSender extends Thread {

    private String message;
    private String receiverBrokerName;
    private QueueBroker queueBroker;
    private MessageQueue messageQueue;

    public MessageSender(String message, String receiverBrokerName) {
        this.message = message;
        this.receiverBrokerName = receiverBrokerName;
    }

    private QueueBroker getQueueBroker() {
        if (queueBroker == null) {
            this.queueBroker = Task.getQueueBroker();
        }
        return this.queueBroker;
    }

    private int getPort() {
        return MessageQueueTest.SENDING_PORT;
    }

    /**
     * Establish connection by creating a MessageQueue to communicate with the receiver.
     */
    public boolean establishConnection() {
        // Use the broker to establish the message queue connection
         boolean connectionRequestSent = getQueueBroker().connect(receiverBrokerName, getPort(), new QueueBroker.ConnectListener() {
            @Override
            public void connected(MessageQueue messageQueue) {
                MessageSender.this.messageQueue = messageQueue;
            }

            @Override
            public void refused() {
                System.out.println("Failed to establish connection in MessageSender");
            }
         });

         return connectionRequestSent;
    }

    /**
     * Sends the message through the MessageQueue.
     */
    private void sendMessage() {
        this.messageQueue.send(message.getBytes(), 0, message.length());
    }


    /**
     * Disconnects the MessageQueue after finishing sending messages.
     */
    public void disconnect() {
        if (messageQueue != null) {
            messageQueue.close();
        }
    }

    /**
     * Simulates sending multiple messages in a loop.
     */
    private void infiniteLoopSending() {
        int nbMessages = MessageQueueTest.NUMBER_OF_MESSAGES;

        while (nbMessages > 0) {
            sendMessage();
            try {
                Thread.sleep(1000); // Simulate a delay between sending messages
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
        if (!connected){
            System.out.println("Failed to establish connection in MessageSender");
            return;
        }
        while (this.messageQueue == null) {
            try {
                Thread.sleep(1000); // Simulate a delay before retrying to connect
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        infiniteLoopSending();  // Start sending messages in a loop
    }
}