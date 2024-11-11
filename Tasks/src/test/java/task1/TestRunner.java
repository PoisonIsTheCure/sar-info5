package task1;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task1.implementation.BrokerImpl;
import task1.implementation.BrokerManager;
import task1.implementation.TaskImpl;
import task1.specification.Broker;
import task1.specification.Task;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * TestRunner class for Task 1.
 * <p>
 * This is an EchoServer that receives a message from a Sender and sends it back to the Sender.
 *
 * There are two tests in this class:
 * <p>
 * <ol>
 * <li>task1GeneralTest: This test creates a Sender and a Receiver and sends a message from the Sender to the Receiver.</li>
 * <li>task1MultipleSenders: This test creates two Senders and a Receiver and sends messages from both Senders to the Receiver.</li>
 * </ol>
 * <p>
 * The tests are run using JUnit 5.
 */
public class TestRunner {

    public static final int SENDING_PORT = 6923;
    public static final int RECEIVING_PORT = 6923;
    public static final int NUMBER_OF_MESSAGES = 10;

    @Test
    public void task1GeneralTest() {
        // Broker names for sender and receiver
        String senderBrokerName = "senderBroker";
        String receiverBrokerName = "receiverBroker";

        BrokerManager manager = new BrokerManager();
        // Create Broker instances
        Broker senderBroker = new BrokerImpl(senderBrokerName, manager);
        Broker receiverBroker = new BrokerImpl(receiverBrokerName, manager);

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

    @Test
    public void task1MultipleSenders(){
        // Broker names for sender and receiver
        String senderBrokerName = "senderBroker";
        String senderBrokerName2 = "senderBroker2";
        String receiverBrokerName = "receiverBroker";

        BrokerManager manager = new BrokerManager();
        // Create Broker instances
        Broker senderBroker = new BrokerImpl(senderBrokerName, manager);
        Broker senderBroker2 = new BrokerImpl(senderBrokerName2, manager);
        Broker receiverBroker = new BrokerImpl(receiverBrokerName, manager);

        // Create TaskImpl instances for sender and receiver
        Task senderTask = new TaskImpl(senderBroker, new Sender("Hello from Sender 1", receiverBrokerName, MultipleReceiver.PORT_1));
        Task senderTask2 = new TaskImpl(senderBroker2, new Sender("Hello from Sender 2", receiverBrokerName, MultipleReceiver.PORT_2));
        Task receiverTask = new TaskImpl(receiverBroker, new MultipleReceiver());

        // Start the tasks
        senderTask.start();
        senderTask2.start();
        receiverTask.start();

        // Wait for both tasks to complete execution
        try {
            senderTask.join();
            senderTask2.join();
            receiverTask.join();
        } catch (InterruptedException e) {
            System.out.println("TestRunner interrupted while waiting for tasks to finish.");
        }

        System.out.println("TestRunner finished tests.");
    }
}
