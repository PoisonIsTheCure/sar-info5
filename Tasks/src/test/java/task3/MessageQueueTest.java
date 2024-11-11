package task3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import task3.implementation.*;
import task3.specification.*;
import org.tinylog.Logger;

import java.util.ArrayList;

public class MessageQueueTest {

    public static final int PORT = 6923;
    public static final int NUMBER_OF_MESSAGES = 10;

    private EventPump eventPump;

    @Test
    public void messageQueueTest() {
        Logger.info("MessageQueueTest started.");
        // Initialize BrokerManager to handle both sender and receiver brokers
        BrokerManager.getInstance();
        Logger.info("BrokerManager initialized.");

        // Initialize the EventPump
        eventPump = new EventPumpImpl();
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
        while (!ETask.runningTasks.isEmpty()) {
            synchronized (ETask.runningTasks) {
                for (ETask task : new ArrayList<>(ETask.runningTasks)) {
                    if (task != null && !task.killed()) {
                        task.run();
                    }
                }
            }
        }

        System.out.println("MessageQueueTest completed successfully.");
    }


    @Test
    public void task3MultipleSendersTest(){
        Logger.info("task3MultipleSendersTest started.");
        // Initialize BrokerManager to handle both sender and receiver brokers
        BrokerManager.getInstance();
        Logger.info("BrokerManager initialized.");

        // Initialize the EventPump
        eventPump = new EventPumpImpl();
        Logger.info("EventPump initialized.");

        // Create QueueBrokers for sender and receiver
        QueueBroker senderQueueBroker = new QueueBrokerImpl("senderBroker");
        QueueBroker senderQueueBroker2 = new QueueBrokerImpl("senderBroker2");
        QueueBroker receiverQueueBroker = new QueueBrokerImpl("receiverBroker");
        Logger.info("QueueBrokers created.");

        // Start the MultipleSendersReceiver thread
        MultipleSendersReceiver multipleSendersReceiver = new MultipleSendersReceiver(receiverQueueBroker, eventPump);
        // Start MessageSender threads
        MessageSender senderTester = new MessageSender("Hello from Sender1!",
                "receiverBroker", eventPump, senderQueueBroker, MultipleSendersReceiver.PORT_1);
        MessageSender senderTester2 = new MessageSender("Hello from Sender2!",
                "receiverBroker", eventPump, senderQueueBroker2, MultipleSendersReceiver.PORT_2);
        Logger.info("MultipleSendersReceiver and Senders Event-based task created.");

        // Start the EventPump
        eventPump.start();
        Logger.info("EventPump started.");

        // Running the tasks
        while (!ETask.runningTasks.isEmpty()) {
            synchronized (ETask.runningTasks) {
                for (ETask task : new ArrayList<>(ETask.runningTasks)) {
                    if (task != null && !task.killed()) {
                        task.run();
                    }
                    else if (task != null && task.killed()){
                        ETask.runningTasks.remove(task);
                    }
                }
            }
        }

        System.out.println("task3MultipleSendersTest completed successfully.");
    }

    @AfterEach
    public void tearDown() {
        eventPump.kill();
        Logger.info("EventPump killed.");

        BrokerManager.getInstance().reset();
    }
}