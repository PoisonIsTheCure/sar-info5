package task3;

import task3.specification.*;

public class MessageReceiver extends ETask implements Runnable{

    private final QueueBroker queueBroker;
    private volatile MessageQueue messageQueue;
    private volatile State state;


    // Message Counters
    private volatile int receivedMessages = 0;
    private final int totalMessagesToSend = MessageQueueTest.NUMBER_OF_MESSAGES;

    enum State {
        INIT,WAITING_CONNECTION,SETTING_LISTENER,CONNECTED, FINISHED, DISCONNECTING, DEAD
    }

    public MessageReceiver(QueueBroker queueBroker, EventPump pump) {
        super(pump);
        this.queueBroker = queueBroker;
        this.state = State.INIT;
    }

    private QueueBroker getQueueBroker() {
        return this.queueBroker;
    }

    private int getPort() {
        return MessageQueueTest.PORT;
    }

    /**
     * Establish connection by creating a MessageQueue to communicate with the sender.
     */
    public void establishConnection() {
        getQueueBroker().bind(getPort(), new QueueBroker.AcceptListener() {
            @Override
            public void accepted(MessageQueue messageQueue) {
                MessageReceiver.this.messageQueue = messageQueue;
            }
        });
    }

    /**
     * Echoes the received message back to the sender.
     */
    private void echoMessage(byte[] message) {
        if (state != State.CONNECTED) {
            return;
        }
        System.out.println("<-- MessageReceiver received message " + receivedMessages);
        receivedMessages++;
        messageQueue.send(message); // Send back the same message
        if (receivedMessages == totalMessagesToSend) {
            this.state = State.FINISHED;
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
                if (messageQueue != null) {
                    state = State.SETTING_LISTENER;
                }
                break;
            case SETTING_LISTENER:
                // Set the Listener for receiving messages
                messageQueue.setListener(new MessageQueue.Listener() {
                    @Override
                    public void received(byte[] msg) {
                        echoMessage(msg);
                    }

                    @Override
                    public void closed() {
                        state = State.DISCONNECTING;
                    }

                });
                state = State.CONNECTED;
                MessageQueueTest.logger.info("MessageReceiver is connected and listening");
                break;

            case FINISHED:
                if (receivedMessages == totalMessagesToSend) {
                    System.out.println("MessageReceiver received and echoed all messages");
                    state = State.DISCONNECTING;
                }
                break;
            case DISCONNECTING:
                MessageQueueTest.logger.info("MessageReceiver is disconnecting");
                queueBroker.unbind(getPort());
                state = State.DEAD;
                break;
            case DEAD:
                MessageQueueTest.logger.info("MessageReceiver is dead");
                this.kill();
                break;
            default:
                break;
        }
    }
}