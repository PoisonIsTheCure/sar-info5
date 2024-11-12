package task4;

import org.tinylog.Logger;
import task4.specification.*;

import java.util.ArrayList;
import java.util.List;

public class MultipleSendersReceiver extends Task {

    private final QueueBroker queueBroker;
    private final List<MessageQueue> messageQueues;
    private State state;

    // Ports to accept
    public static final int PORT_1 = 5000;
    public static final int PORT_2 = 5001;



    enum State {
        INIT,WAITING_CONNECTION,SETTING_LISTENER,CONNECTED, FINISHED, DISCONNECTING, DEAD
    }

    public MultipleSendersReceiver(QueueBroker queueBroker, EventPump pump) {
        super(pump);
        this.queueBroker = queueBroker;
        this.state = State.INIT;
        this.messageQueues = new ArrayList<>();
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
                messageQueues.add(messageQueue);
            }
        });

        getQueueBroker().bind(PORT_2, new QueueBroker.AcceptListener() {
            @Override
            public void accepted(MessageQueue messageQueue) {
                messageQueues.add(messageQueue);
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
        // Recreate the message
        Message msg = new Message(message, 0, message.length);

        Logger.debug("MessageReceiver received message: " + ChecksumUtility.getMessageContent(msg));

        // Check if the message is the last one
        if (ChecksumUtility.isCloseMessage(msg)) {
            // This Check Need to be done before the Checksum
            // Because the default Checksum is not valid for the Close Message
            state = State.FINISHED;
            return;
        }

        // Run the Checksum
        if (!ChecksumUtility.verifyReceivedMessage(msg)){
            Logger.info("Checksum failed for message: " + ChecksumUtility.getMessageContent(msg));
            return;
        }

        // Echo the message back
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
                if (messageQueues.size() == 2) {
                    state = State.SETTING_LISTENER;
                }
                break;
            case SETTING_LISTENER:
                // Set the Listener for receiving messages
                for (MessageQueue messageQueue : messageQueues) {
                    messageQueue.setListener(new MessageQueue.Listener() {
                        @Override
                        public void received(byte[] msg) {
                            echoMessage(msg, messageQueue);
                        }

                        @Override
                        public void sent(Message msg) {
                            Logger.info("MessageReceiver echoed message");
                        }

                        @Override
                        public void closed() {
                            state = State.DISCONNECTING;
                        }

                    });
                }
                // In the connected state, the receiver is listening for messages
                state = State.CONNECTED;
                Logger.info("MessageReceiver is connected and listening");
                break;
            case FINISHED:
                Logger.info("MessageReceiver received and echoed all messages");
                state = State.DISCONNECTING;
                // Fall through
            case DISCONNECTING:
                Logger.info("MessageReceiver is disconnecting");
                queueBroker.unbind(PORT_1);
                queueBroker.unbind(PORT_2);
                state = State.DEAD;
                // Fall through
            case DEAD:
                Logger.info("MessageReceiver is dead");
                this.kill();
                break;
            default:
                break;
        }
    }
}