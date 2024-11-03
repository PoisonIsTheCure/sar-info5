package task4;

import org.junit.jupiter.api.*;
import org.tinylog.Logger;
import task4.events.GeneralEvent;
import task4.implementation.*;
import task4.specification.*;

import javax.annotation.processing.Messager;
import java.util.ArrayList;

public class MessageQueueTest {

    public static final int PORT = 6923;
    public static final int NUMBER_OF_MESSAGES = 10;
    private static final int receiverQueueBrokersID = 0;
    private static final int senderQueueBrokersID = 0;


    private EventPump eventPump;

    @BeforeEach
    public void setUp() {
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

    }

    @Test
    public void testMessageQueue() {
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
                    if (task instanceof MessageSender && ((MessageSender) task).receivedMessages == NUMBER_OF_MESSAGES) {
                        ((MessageSender) task).setFinished();
                    }
                }
            }
        }

        eventPump.post(new GeneralEvent(null,() -> {
            Logger.info("MessageQueueTest: EventPump is killed.");
            eventPump.kill();
        }));

        // Assert that both sender and receiver have transitioned to DEAD state
//        Assertions.assertTrue(senderTask.isDead(), "Sender should be dead");
//        Assertions.assertTrue(receiverTask.isDead(), "Receiver should be dead");

    }

}