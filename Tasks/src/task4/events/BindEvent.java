package task4.events;

import task4.specification.*;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class BindEvent implements Event {

    // Lists of currently active acceptors
    static final ConcurrentHashMap<String, HashMap<Integer, Task>> acceptors = new ConcurrentHashMap<>();

    private int port;
    private QueueBroker.AcceptListener listener;
    private Broker broker;
    public BindEvent(Broker broker,int port, QueueBroker.AcceptListener listener) {
        this.broker = broker;
        this.port = port;
        this.listener = listener;
    }


    // BindEvent react function Implementation
    @Override
    public void react() {
        // TODO: Implement this method
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return broker.getName();
    }
}
