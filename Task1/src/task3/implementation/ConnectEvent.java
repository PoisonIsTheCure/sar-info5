package task3.implementation;

import task3.specification.*;

import java.io.IOException;

public class ConnectEvent implements Event {

    private String name;
    private int port;
    private QueueBroker.ConnectListener listener;
    private Broker requestBroker;

    public ConnectEvent(String name , int port, QueueBroker.ConnectListener listener, Broker requestBroker) {
        this.name = name;
        this.port = port;
        this.listener = listener;
        this.requestBroker = requestBroker;
    }

    @Override
    public void react() {
        new TaskImpl(() -> {
            try {
                Channel channel = requestBroker.connect(name, port);
                MessageQueue messageQueue = new MessageQueueImpl(channel);
                listener.connected(messageQueue);
            } catch (IOException e) {
                listener.refused();
            }
        }).start();
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }
}
