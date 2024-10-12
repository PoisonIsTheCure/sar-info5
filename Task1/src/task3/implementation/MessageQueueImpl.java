package task3.implementation;

import task3.specification.*;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

public class MessageQueueImpl extends MessageQueue {

    private final Channel channel;
    private volatile boolean isClosed;
    private ETask parentTask;

    // Threaded workers
    private final MQSender senderWorker;
    private final MQReceiver receiverWorker;


    public MessageQueueImpl(Channel channel) {
        this.channel = channel;
        this.isClosed = false;
        this.senderWorker = new MQSender(this, channel);
        this.receiverWorker = new MQReceiver(this, channel);

        // Start the sender and receiver threads
        new TaskImpl(senderWorker).start();
        new TaskImpl(receiverWorker).start();
    }

    @Override
    public void setListener(MessageQueue.Listener l) {
        this.listener = l;
    }

    @Override
    public void send(byte[] bytes, int offset, int length) throws DisconnectedException {
        if (isClosed) {
            throw new DisconnectedException("MessageQueue is closed. Cannot send messages.");
        }
        senderWorker.send(bytes, offset, length);
    }

    @Override
    public void send(byte[] bytes) throws DisconnectedException {
        send(bytes, 0, bytes.length);
    }

    @Override
    public void close() {
        isClosed = true;
        channel.disconnect();  // Disconnect the channel
    }

    @Override
    public boolean closed() {
        return isClosed && channel.disconnected();
    }

    @Override
    public Listener getListener() {
        return listener;
    }

}