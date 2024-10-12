package task3.tests;

import task3.implementation.*;
import task3.specification.Broker;
import task3.specification.EventPump;
import task3.specification.QueueBroker;
import task3.specification.Task;

public class MessageQueueTest {

    public static final int SENDING_PORT = 6923;
    public static final int RECEIVING_PORT = 6923;
    public static final int NUMBER_OF_MESSAGES = 10;

    public static void main(String[] args) {
        // Initialize BrokerManager to handle both sender and receiver brokers
        BrokerManager.getInstance();

        // Initialize the EventPump
       EventPump eventPump = new EventPumpImpl();

        // Create QueueBrokers for sender and receiver
        QueueBroker senderQueueBroker = new QueueBrokerImpl("senderBroker");
        QueueBroker receiverQueueBroker = new QueueBrokerImpl("receiverBroker");

        // Start the MessageReceiver and MessageSender threads
        MessageReceiver receiverTester = new MessageReceiver();
        MessageSender senderTester = new MessageSender("Hello from MessageQueueTest!", "receiverBroker", eventPump);


        // Wait for both threads to complete their tasks
        try {
            senderTask.join();
            receiverTask.join();
        } catch (InterruptedException e) {
            System.out.println("MessageQueueTest interrupted while waiting for threads to finish.");
        }

        System.out.println("MessageQueueTest completed successfully.");
    }
}