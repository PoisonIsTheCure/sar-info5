package task3.tests;

import task3.implementation.*;
import task3.specification.*;

import java.util.logging.Logger;

public class MessageQueueTest {

    public static final int PORT = 6923;
    public static final int NUMBER_OF_MESSAGES = 10;
    public static final Logger logger = Logger.getLogger(MessageQueueTest.class.getName());

    public static void main(String[] args) {

        logger.info("MessageQueueTest started.");
        // Initialize BrokerManager to handle both sender and receiver brokers
        BrokerManager.getInstance();
        logger.info("BrokerManager initialized.");

        // Initialize the EventPump
        EventPump eventPump = new EventPumpImpl();
        logger.info("EventPump initialized.");

        // Create QueueBrokers for sender and receiver
        QueueBroker senderQueueBroker = new QueueBrokerImpl("senderBroker");
        QueueBroker receiverQueueBroker = new QueueBrokerImpl("receiverBroker");
        logger.info("QueueBrokers created.");

        // Start the MessageReceiver and MessageSender threads
        MessageReceiver receiverTester = new MessageReceiver(receiverQueueBroker, eventPump);
        MessageSender senderTester = new MessageSender("Hello from MessageQueueTest!",
                "receiverBroker", eventPump, senderQueueBroker);
        logger.info("MessageReceiver and MessageSender Event-based tasks created.");


        // Start the EventPump
        eventPump.start();
        logger.info("EventPump started.");

        // Running the tasks
        while (!ETask.runningTasks.isEmpty()) {
            int size = ETask.runningTasks.size();
            for (int i = 0; i < size; i++) {
                // Get the current task
                ETask task;
                try {
                    task = ETask.runningTasks.get(i);
                } catch (IndexOutOfBoundsException e) {
                    // In case Task has been removed from the list (By EventPump)
                    break;
                }

                // Run the task
                if (task !=null && !task.killed()) {
                    task.run();
                }
            }
        }

        eventPump.kill();
        System.out.println("MessageQueueTest completed successfully.");
    }
}