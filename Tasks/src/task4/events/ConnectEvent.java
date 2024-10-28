package task4.events;

import task4.specification.*;

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
        // TODO: Implement this method
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }
}
