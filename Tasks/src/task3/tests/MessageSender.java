package task3.tests;

import task3.specification.*;

public class MessageSender extends ETask implements Runnable {

    private final QueueBroker queueBroker;
    private volatile MessageQueue messageQueue;
    private final String receiverBrokerName;
    private final String message;
    private volatile State state;
    private boolean testPassed = true;

    // Message Counters
    private int receivedMessages = 0;
    private int sentMessages = 0;
    private int totalMessagesToSend = MessageQueueTest.NUMBER_OF_MESSAGES;

    enum State {
        INIT, WAITING_CONNECTION, SETTING_LISTENER, FINISHED, CONNECTED, DISCONNECTING, DEAD
    }

    public MessageSender(String message, String receiverBrokerName, EventPump pump, QueueBroker queueBroker) {
        super(pump);
        this.queueBroker = queueBroker;
        this.receiverBrokerName = receiverBrokerName;
        this.message = message;
        this.state = State.INIT;
    }

    private QueueBroker getQueueBroker() {
        return this.queueBroker;
    }

    private int getPort() {
        return MessageQueueTest.PORT;
    }

    /**
     * Establish connection by creating a MessageQueue to communicate with the receiver.
     */
    public void establishConnection() {
        getQueueBroker().connect(receiverBrokerName, getPort(), new QueueBroker.ConnectListener() {
            @Override
            public void connected(MessageQueue messageQueue) {
                MessageSender.this.messageQueue = messageQueue;
            }

            @Override
            public void refused() {
                System.out.println("Failed to establish connection in MessageSender");
                state = State.DISCONNECTING;
            }
        });
    }

    /**
     * Sends the message through the MessageQueue.
     */
    private void sendMessage() {
        if (sentMessages == totalMessagesToSend) {
            state = State.FINISHED;
            MessageQueueTest.logger.info("MessageSender finished sending messages");
            return;
        }
        if (state == State.CONNECTED && messageQueue != null && sentMessages < totalMessagesToSend) {
            System.out.println("--> Sending message "+ this.sentMessages +" : " + message);
            this.sentMessages++;
            messageQueue.send(message.getBytes());
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
                // Set the listener for receiving any responses or connection status changes
                messageQueue.setListener(new MessageQueue.Listener() {
                    @Override
                    public void received(byte[] msg) {
                        testPassed = testPassed && new String(msg).equals(message);
                        System.out.println("--> Echo "+ receivedMessages +" received and matched");
                        receivedMessages++;
                    }

                    @Override
                    public void closed() {
                        state = State.DISCONNECTING;
                    }
                });
                state = State.CONNECTED;
                MessageQueueTest.logger.info("MessageSender is connected and listening");
                break;

            case CONNECTED:
                sendMessage(); // Send message only when connected
                break;

            case FINISHED:
                if (testPassed && receivedMessages == totalMessagesToSend) {
                    System.out.println("MessageSender test passed");
                    state = State.DISCONNECTING;
                }
                break;

            case DISCONNECTING:
                MessageQueueTest.logger.info("MessageSender is disconnecting");
                state = State.DEAD;
                break;
            case DEAD:
                MessageQueueTest.logger.info("MessageSender is dead");
                this.kill();
                break;
            default:
                break;
        }
    }
}