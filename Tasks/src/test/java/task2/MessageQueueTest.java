package task2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import task2.implementation.BrokerImpl;
import task2.implementation.BrokerManager;
import task2.implementation.QueueBrokerImpl;
import task2.implementation.TaskImpl;
import task2.specification.Broker;
import task2.specification.QueueBroker;
import task2.specification.Task;

public class MessageQueueTest {

    public static final int SENDING_PORT = 6923;
    public static final int RECEIVING_PORT = 6923;
    public static final int NUMBER_OF_MESSAGES = 10;

    @Test
    public void task2GeneralTest() {
        // Initialize BrokerManager to handle both sender and receiver brokers
        BrokerManager brokerManager = new BrokerManager();

        // Create Brokers for sender and receiver
        Broker senderBroker = new BrokerImpl("senderBroker", brokerManager);
        Broker receiverBroker = new BrokerImpl("receiverBroker", brokerManager);

        // Create QueueBrokers for sender and receiver
        QueueBroker senderQueueBroker = new QueueBrokerImpl(senderBroker);
        QueueBroker receiverQueueBroker = new QueueBrokerImpl(receiverBroker);

        // Start the MessageReceiver and MessageSender threads
        MessageReceiver receiverTester = new MessageReceiver();
        MessageSender senderTester = new MessageSender("Hello from task2 General Test!", "receiverBroker");

        // Run the receiver in a separate thread
        Task receiverTask = new TaskImpl(receiverQueueBroker, receiverTester);
        receiverTask.start();

        // Run the sender in a separate thread
        Task senderTask = new TaskImpl(senderQueueBroker, senderTester);
        senderTask.start();

        // Wait for both threads to complete their tasks
        try {
            senderTask.join();
            receiverTask.join();
        } catch (InterruptedException e) {
            Assertions.fail("MessageQueueTest interrupted while waiting for threads to finish.");
        }

        System.out.println("MessageQueueTest completed successfully.");
    }

    @Test
    public void task2MultipleSenderReceiverTest() {
        // Initialize BrokerManager to handle both sender and receiver brokers
        BrokerManager brokerManager = new BrokerManager();

        // Create Brokers for sender and receiver
        Broker senderBroker = new BrokerImpl("senderBroker", brokerManager);
        Broker senderBroker2 = new BrokerImpl("senderBroker2", brokerManager);
        Broker receiverBroker = new BrokerImpl("receiverBroker", brokerManager);

        // Create QueueBrokers for sender and receiver
        QueueBroker senderQueueBroker = new QueueBrokerImpl(senderBroker);
        QueueBroker senderQueueBroker2 = new QueueBrokerImpl(senderBroker2);
        QueueBroker receiverQueueBroker = new QueueBrokerImpl(receiverBroker);

        // Start the MultipleSenderReceiver threads
        MultipleSenderReceiver multipleSenderReceiver = new MultipleSenderReceiver();

        // Run the receiver in a separate thread
        Task receiverTask = new TaskImpl(receiverQueueBroker, multipleSenderReceiver);
        receiverTask.start();

        // Run the sender in a separate thread
        Task senderTask = new TaskImpl(senderQueueBroker, new MessageSender("Hello from task2 1st Sender Test!", "receiverBroker", MultipleSenderReceiver.PORT_1));
        Task senderTask2 = new TaskImpl(senderQueueBroker2, new MessageSender("Hello from task2 2nd Sender Test!", "receiverBroker", MultipleSenderReceiver.PORT_2));
        senderTask.start();
        senderTask2.start();

        // Wait for both threads to complete their tasks
        try {
            senderTask.join();
            senderTask2.join();
            receiverTask.join();
        } catch (InterruptedException e) {
            Assertions.fail("MessageQueueTest interrupted while waiting for threads to finish.");
        }

        System.out.println("MessageQueueTest completed successfully.");
    }
}