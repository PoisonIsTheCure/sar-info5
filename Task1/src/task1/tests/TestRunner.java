package task1.tests;

import task1.implementation.BrokerImpl;
import task1.implementation.TaskImpl;
import task1.specification.Broker;
import task1.specification.Task;

public class TestRunner {

    public static final int SENDING_PORT = 6923;
    public static final int RECEIVING_PORT = 6924;
    public static final int NUMBER_OF_MESSAGES = 1000;

    public static void main(String[] args) {
        // Broker names for sender and receiver
        String senderBrokerName = "senderBroker";
        String receiverBrokerName = "receiverBroker";

        // Create Broker instances
        Broker senderBroker = new BrokerImpl(senderBrokerName);
        Broker receiverBroker = new BrokerImpl(receiverBrokerName);

        // Create TaskImpl instances for sender and receiver
        Task senderTask = new TaskImpl(senderBroker, new Sender("Hello, World!", receiverBrokerName));
        Task receiverTask = new TaskImpl(receiverBroker, new Receiver());

        // Start the tasks
        senderTask.start();
        receiverTask.start();

        // Wait for both tasks to complete execution
        try {
            senderTask.join();
            receiverTask.join();
        } catch (InterruptedException e) {
            System.out.println("TestRunner interrupted while waiting for tasks to finish.");
        }

        System.out.println("TestRunner finished tests.");
    }
}