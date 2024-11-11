package task3;

import org.tinylog.Logger;
import task3.specification.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MultipleSendersReceiver extends ETask implements Runnable{

    private final QueueBroker queueBroker;

    // HashMap of MessageQueues as keys and nb of messages received as values
    private final HashMap<MessageQueue, Integer> messageQueues = new HashMap<>();
    private volatile State state;

    // ports
    public static final int PORT_1 = 5000;
    public static final int PORT_2 = 5001;


    // Message Counters
    private volatile int receivedMessages = 0;
    private final int totalMessagesToSend = MessageQueueTest.NUMBER_OF_MESSAGES;

    enum State {
        INIT,WAITING_CONNECTION,SETTING_LISTENER,CONNECTED, FINISHED, DISCONNECTING, DEAD
    }

    public MultipleSendersReceiver(QueueBroker queueBroker, EventPump pump) {
        super(pump);
        this.queueBroker = queueBroker;
        this.state = State.INIT;
    }

    private QueueBroker getQueueBroker() {
        return this.queueBroker;
    }

    /**
     * Establish connection by creating a MessageQueue to communicate with the sender.
     */
    public void establishConnection() {
        getQueueBroker().bind(PORT_1, new QueueBroker.AcceptListener() {
            @Override
            public void accepted(MessageQueue messageQueue) {
                synchronized (messageQueues) {
                    messageQueues.put(messageQueue, totalMessagesToSend);
                }
            }
        });

        getQueueBroker().bind(PORT_2, new QueueBroker.AcceptListener() {
            @Override
            public void accepted(MessageQueue messageQueue) {
                synchronized (messageQueues) {
                    messageQueues.put(messageQueue, totalMessagesToSend);
                }
            }
        });
    }

    /**
     * Echoes the received message back to the sender.
     */
    private void echoMessage(byte[] message, MessageQueue messageQueue) {
        if (state != State.CONNECTED) {
            return;
        }
        receivedMessages = MessageQueueTest.NUMBER_OF_MESSAGES - messageQueues.get(messageQueue);
        System.out.println("<-- MultipleSendersReceiver received message " + receivedMessages);


        messageQueues.put(messageQueue, messageQueues.get(messageQueue) - 1);
        messageQueue.send(message); // Send back the same message
        if (messageQueues.get(messageQueue) == 0) {
            messageQueues.remove(messageQueue);
        }
        if (messageQueues.isEmpty()){
            state = State.FINISHED;
        }
    }

    @Override
    public void run() {
        switch (state) {
            case INIT:
                establishConnection();
                state = State.WAITING_CONNECTION;
                break;
            case WAITING_CONNECTION:
                if (messageQueues.size() == 2) {
                    state = State.SETTING_LISTENER;
                }
                break;
            case SETTING_LISTENER:
                // Set the Listener for receiving messages
                for (MessageQueue messageQueue : messageQueues.keySet()) {
                    messageQueue.setListener(new MessageQueue.Listener() {
                        @Override
                        public void received(byte[] msg) {
                            echoMessage(msg, messageQueue);
                        }

                        @Override
                        public void closed() {
                            synchronized (messageQueues) {
                                messageQueues.remove(messageQueue);
                            }
                            if (messageQueues.isEmpty()) {
                                // All senders disconnected
                                state = State.DISCONNECTING;
                            }
                        }
                    });
                }
                state = State.CONNECTED;
                Logger.info("MultipleSendersReceiver is connected and listening");
                break;

            case FINISHED:
                if (messageQueues.isEmpty()) {
                    System.out.println("MultipleSendersReceiver received and echoed all messages");
                    state = State.DISCONNECTING;
                }
                break;
            case DISCONNECTING:
                Logger.info("MultipleSendersReceiver is disconnecting");
                queueBroker.unbind(PORT_1);
                queueBroker.unbind(PORT_2);
                state = State.DEAD;
                break;
            case DEAD:
                Logger.info("MultipleSendersReceiver is dead");
                this.kill();
                break;
            default:
                break;
        }
    }
}