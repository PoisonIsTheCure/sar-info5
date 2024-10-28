package task4.tests;

import org.tinylog.Logger;
import task4.events.GeneralEvent;
import task4.implementation.*;
import task4.specification.*;

import java.util.ArrayList;

public class MessageQueueTest {

    public static final int PORT = 6923;
    public static final int NUMBER_OF_MESSAGES = 10;

    public static void main(String[] args) {

        // Logger check:
        Logger.info("Loaded configuration from: {}", System.getProperty("tinylog.configuration"));

        Logger.info("MessageQueueTest started.");
        // Initialize BrokerManager to handle both sender and receiver brokers
        BrokerManager.getInstance();
        Logger.info("BrokerManager initialized.");

        // Initialize the EventPump
        EventPump eventPump = new EventPumpImpl();
        Logger.info("EventPump initialized.");

        // Create QueueBrokers for sender and receiver
        QueueBroker senderQueueBroker = new QueueBrokerImpl("senderBroker");
        QueueBroker receiverQueueBroker = new QueueBrokerImpl("receiverBroker");
        Logger.info("QueueBrokers created.");

        // Start the MessageReceiver and MessageSender threads
        MessageReceiver receiverTester = new MessageReceiver(receiverQueueBroker, eventPump);
        MessageSender senderTester = new MessageSender("Hello from MessageQueueTest!",
                "receiverBroker", eventPump, senderQueueBroker);
        Logger.info("MessageReceiver and MessageSender Event-based tasks created.");


        // Start the EventPump
        eventPump.start();
        Logger.info("EventPump started.");


        // Running the tasks
        while (!Task.runningTasks.isEmpty()) {
            synchronized (Task.runningTasks) {
                for (Task task : new ArrayList<>(Task.runningTasks)) {
                    if (task != null && !task.killed()) {
                        task.run();
                    }
                }
            }
        }

        eventPump.post(new GeneralEvent(() -> {
            Logger.info("MessageQueueTest: EventPump is killed.");
            eventPump.kill();
        }));

        System.out.println("MessageQueueTest completed successfully.");
    }
}