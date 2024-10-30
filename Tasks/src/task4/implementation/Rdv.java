package task4.implementation;

import task4.CircularBuffer;
import task4.specification.Channel;
import task4.specification.MessageQueue;
import task4.specification.QueueBroker;

import java.nio.Buffer;

public class Rdv {
    private final static int BUFFER_SIZE = 10;

    // Channels
    private ChannelImpl channel1;
    private ChannelImpl channel2;

    // MessageQueues
    private MessageQueueImpl messageQueue1;
    private MessageQueueImpl messageQueue2;

    public Rdv() {
    }

    /**
     * This function is called by the destination broker (who received the connection request)
     * The function establishes the connection between the two brokers, having the 2 listeners (Connect and Accept)
     *
     * The function is called in the EventPump thread (by AcceptEvent), so no concurrency issues
     * @param connectListener The listener of the source broker
     * @param acceptListener The listener of the destination broker
     * @return
     */
    public boolean createRdv(QueueBroker.ConnectListener connectListener, QueueBroker.AcceptListener acceptListener) {

        // Create buffers
        CircularBuffer buffer1 = new CircularBuffer(BUFFER_SIZE);
        CircularBuffer buffer2 = new CircularBuffer(BUFFER_SIZE);

        // Create Channels
        this.channel1 = new ChannelImpl(this, buffer1, buffer2);
        this.channel2 = new ChannelImpl(this, buffer2, buffer1);

        // Create MessageQueues
        this.messageQueue1 = new MessageQueueImpl(this, channel1);
        this.messageQueue2 = new MessageQueueImpl(this, channel2);

        // Notify the listeners
        connectListener.connected(messageQueue1);
        acceptListener.accepted(messageQueue2);

        return true;
    }

    public ChannelImpl getChannel1() {
        return channel1;
    }

    public ChannelImpl getChannel2() {
        return channel2;
    }

    public MessageQueueImpl getMessageQueue1() {
        return messageQueue1;
    }

    public MessageQueueImpl getMessageQueue2() {
        return messageQueue2;
    }
}
