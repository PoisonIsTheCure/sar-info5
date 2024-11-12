package task4;

import org.junit.jupiter.api.*;
import org.tinylog.Logger;
import task4.events.GeneralEvent;
import task4.implementation.*;
import task4.specification.*;

import java.util.ArrayList;

public class MessageQueueTest {

    public static final int PORT = 6923;
    public static final int NUMBER_OF_MESSAGES = 10;
    private static int receiverQueueBrokersID = 0;
    private static int senderQueueBrokersID = 0;

    private EventPump eventPump;

    @BeforeEach
    public void setUp() {
        // Initialize BrokerManager to handle both sender and receiver brokers
        BrokerManager.getInstance();
        Logger.info("BrokerManager initialized.");

        // Initialize the EventPump
        eventPump = new EventPumpImpl();
        Logger.info("EventPump initialized.");
    }

    @Test
    public void task4GeneralTest() {
        // Start the EventPump
        eventPump.start();
        Logger.info("EventPump started.");

        // Create initial sender and receiver tasks
        Task receiverTask = createNewReceiverTask();
        Task senderTask = createNewSenderTask();
        Logger.info("MessageReceiver and MessageSender Event-based tasks created.");

        // Running the tasks
        while (!Task.runningTasks.isEmpty()) {
            for (Task task : new ArrayList<>(Task.runningTasks)) {
                if (task != null && !task.killed()) {
                    task.run();
                }
//                if (task instanceof MessageSender && ((MessageSender) task).sentMessages == NUMBER_OF_MESSAGES) {
//                    ((MessageSender) task).setFinished();
//                }
            }
        }

        eventPump.post(new GeneralEvent(null, () -> {
            Logger.info("MessageQueueTest: EventPump is killed.");
            eventPump.kill();
        }));

        // Assert that both sender and receiver have transitioned to DEAD state
        Assertions.assertTrue(senderTask.killed(), "Sender should be dead");
        Assertions.assertTrue(receiverTask.killed(), "Receiver should be dead");
    }

    @Test
    public void task4MultipleSendersTest() {
        // Start the EventPump
        eventPump.start();
        Logger.info("EventPump started.");

        // Create QueueBrokers for sender and receiver
        QueueBroker senderQueueBroker1 = new QueueBrokerImpl("sender1");
        QueueBroker senderQueueBroker2 = new QueueBrokerImpl("sender2");
        QueueBroker receiverQueueBroker = new QueueBrokerImpl("receiver");

        // Create the sender and receiver tasks
        Task senderTask1 = new MessageSender("receiver",eventPump, senderQueueBroker1, MultipleSendersReceiver.PORT_1);
        Task senderTask2 = new MessageSender("receiver",eventPump, senderQueueBroker2, MultipleSendersReceiver.PORT_2);
        Task receiverTask = new MultipleSendersReceiver(receiverQueueBroker, eventPump);
        Logger.info("2 Sender and 1 Receiver --> Event-based tasks created.");

        // Running the tasks
        while (!Task.runningTasks.isEmpty()) {
            for (Task task : new ArrayList<>(Task.runningTasks)) {
                if (task != null && !task.killed()) {
                    task.run();
                }
//                if (task instanceof MessageSender && ((MessageSender) task).sentMessages == NUMBER_OF_MESSAGES) {
//                    ((MessageSender) task).setFinished();
//                }
            }
        }

        eventPump.post(new GeneralEvent(null, () -> {
            Logger.info("MessageQueueTest: EventPump is killed.");
            eventPump.kill();
        }));

        // Assert that both sender and receiver have transitioned to DEAD state
        Assertions.assertTrue(senderTask1.killed(), "Sender 1 should be dead");
        Assertions.assertTrue(senderTask2.killed(), "Sender 2 should be dead");
        Assertions.assertTrue(receiverTask.killed(), "Receiver should be dead");
    }

    @AfterEach
    public void tearDown() {
        eventPump.kill();
        Logger.info("EventPump killed.");

        BrokerManager.getInstance().reset();
    }

    /**
     * Create the next sender Task (using the Id name)
     * Each new sender will be made by a QueueBroker Named send-{nextID}
     */
    private Task createNewSenderTask() {
        // Generate the next QueueBroker name using the next sender ID
        String queueBrokerName = "send-" + senderQueueBrokersID++;

        // Create a new QueueBroker with the generated name
        QueueBroker senderQueueBroker = new QueueBrokerImpl(queueBrokerName);
        Logger.info("Created QueueBroker: " + queueBrokerName);

        // Create a new MessageSender task with appropriate parameters
        MessageSender senderTask = new MessageSender(eventPump, senderQueueBroker);
        Logger.info("MessageSender task created for QueueBroker: " + queueBrokerName);

        return senderTask;
    }

    private Task createNewReceiverTask() {
        // Generate the next QueueBroker name using the next receiver ID
        String queueBrokerName = "receive-" + receiverQueueBrokersID++;

        // Create a new QueueBroker with the generated name
        QueueBroker receiverQueueBroker = new QueueBrokerImpl(queueBrokerName);
        Logger.info("Created QueueBroker: " + queueBrokerName);

        // Create a new MessageReceiver task with appropriate parameters
        MessageReceiver receiverTask = new MessageReceiver(receiverQueueBroker, eventPump);
        Logger.info("MessageReceiver task created for QueueBroker: " + queueBrokerName);

        return receiverTask;
    }
}
