package task3.implementation;

import task3.specification.*;

import java.io.IOException;

public class BindEvent implements Event {
    private int port;
    private QueueBroker.AcceptListener listener;
    private Broker broker;
    public BindEvent(Broker broker,int port, QueueBroker.AcceptListener listener) {
        this.broker = broker;
        this.port = port;
        this.listener = listener;
    }


    @Override
    public void react() {
        // Create a Broker with the given port
        try {
            Channel channel = broker.accept(port);
            MessageQueue messageQueue = new MessageQueueImpl(channel);
            listener.accepted(messageQueue);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Call the listener
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return broker.getName();
    }
}
