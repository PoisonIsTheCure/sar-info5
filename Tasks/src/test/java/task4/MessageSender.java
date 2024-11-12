package task4;

import org.tinylog.Logger;
import task4.specification.*;

public class MessageSender extends Task {

    private final QueueBroker queueBroker;
    private volatile MessageQueue messageQueue;
    private volatile State state;
    private String toConnectWith = null;

    // Message Counters
    public int receivedMessages = 0;
    public volatile int sentMessages = 0;

    // Port
    private int port = MessageQueueTest.PORT;

    enum State {
        INIT, WAITING_CONNECTION, SETTING_LISTENER, FINISHED, CONNECTED, DISCONNECTING, DEAD
    }

    public MessageSender(EventPump pump, QueueBroker queueBroker) {
        super(pump);
        this.queueBroker = queueBroker;
        this.state = State.INIT;
    }

    public MessageSender(String toConnectWith,EventPump pump, QueueBroker queueBroker, int port) {
        super(pump);
        this.toConnectWith = toConnectWith;
        this.queueBroker = queueBroker;
        this.state = State.INIT;
        this.port = port;
    }

    private QueueBroker getQueueBroker() {
        return this.queueBroker;
    }

    private int getPort() {
        return this.port;
    }

    /**
     * Establish connection by creating a MessageQueue to communicate with the receiver.
     */
    public void establishConnection() {

        // Create the receiver name based on sender Name (replacing send with receive in the QueueBroker Name)
        String receiverName = this.queueBroker.name().replace("send","receive");
        if (this.toConnectWith != null){
            receiverName = this.toConnectWith;
        }

        getQueueBroker().connect(receiverName, getPort(), new QueueBroker.ConnectListener() {
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
        sentMessages += 1;
        messageQueue.send(msg);

        // Check the number of messages sent and stop sending messages if the limit is reached
        if (sentMessages == MessageQueueTest.NUMBER_OF_MESSAGES) {
            state = State.FINISHED;
        }
    }


    /**
     * Send Close message to the MessageQueue and disconnect from the receiver.
     */
    private void sendDisconnectMessage(){
        Message msg = ChecksumUtility.createCloseMessageWithChecksum();
        messageQueue.send(msg);
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
                        if (ChecksumUtility.verifyReceivedMessage(new Message(msg,0,msg.length))){
                           System.out.println("--> Echo "+ receivedMessages +" received and matched ("+ queueBroker.name() +")");
                        } else {
                            Logger.error("--> Echo "+ receivedMessages +" didn't Match");
                        }
                        receivedMessages++;
                    }

                    @Override
                    public void sent(Message msg) {
                        System.out.println("--> MessageSender sent message by ("+ queueBroker.name() +")");
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