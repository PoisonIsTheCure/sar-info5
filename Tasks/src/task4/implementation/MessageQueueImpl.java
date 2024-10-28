package task4.implementation;

import task4.specification.*;

public class MessageQueueImpl extends MessageQueue {

    private final Channel channel;
    private volatile boolean isClosed;
    private Task parentTask;


    public MessageQueueImpl(Channel channel) {
        this.channel = channel;
        this.isClosed = false;
    }

    @Override
    public void setListener(MessageQueue.Listener l) {
        this.listener = l;
    }

    @Override
    public void send(byte[] bytes, int offset, int length) throws DisconnectedException {
        // TODO: Implement this method
    }

    @Override
    public void send(byte[] bytes) throws DisconnectedException {
        send(bytes, 0, bytes.length);
    }

    @Override
    public void close() {
        // TODO: Implement this method
    }

    @Override
    public boolean closed() {
        return isClosed;
    }

    @Override
    public Listener getListener() {
        return listener;
    }

}