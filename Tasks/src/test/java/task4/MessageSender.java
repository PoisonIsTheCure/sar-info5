package task4;

import org.tinylog.Logger;
import task4.specification.*;

public class MessageSender extends Task {

    private final QueueBroker queueBroker;
    private volatile MessageQueue messageQueue;
    private final String receiverBrokerName;
    private final String message;
    private volatile State state;
    private boolean testPassed = true;

    // Message Counters
    public int receivedMessages = 0;
    public int sentMessages = 0;

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
                Logger.info("Failed to establish connection in MessageSender");
                state = State.DISCONNECTING;
            }
        });
    }

    /**
     * Sends the message through the MessageQueue.
     */
    private void sendMessage() {
        // Send a random message
        if (state != State.CONNECTED) {
            return;
        }
        Message msg = ChecksumUtility.generateRandomMessageWithChecksum();
        sentMessages++;
        messageQueue.send(msg);
    }


    /**
     * Send Close message to the MessageQueue and disconnect from the receiver.
     */
    private void sendDisconnectMessage(){
        if (state != State.CONNECTED) {
            return;
        }
        Message msg = ChecksumUtility.createCloseMessageWithChecksum();
    }

    public void setFinished() {
        state = State.FINISHED;
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
                        Logger.info("--> Echo "+ receivedMessages +" received and matched");
                        receivedMessages++;
                    }

                    @Override
                    public void sent(Message msg) {
                        Logger.info("--> MessageSender sent message");
                    }


                    @Override
                    public void closed() {
                        state = State.DISCONNECTING;
                    }
                });
                state = State.CONNECTED;
                Logger.info("MessageSender is connected and listening");
                break;

            case CONNECTED:
                sendMessage(); // Send message only when connected
                break;

            case FINISHED:
                Logger.info("MessageSender finished sending messages");
                sendDisconnectMessage();
                state = State.DEAD;
                // Fall through
            case DEAD:
                Logger.info("MessageSender is dead");
                this.kill();
                break;
            default:
                break;
        }
    }
}