package task4.events;

import task4.specification.Broker;
import task4.specification.Event;

public class UnBindEvent implements Event {
    private int port;
    private Broker broker;

    public UnBindEvent(Broker broker,int port) {
        this.broker = broker;
        this.port = port;
    }
    @Override
    public void react() {
        // TODO: Implement this method
    }
    public int getPort() {
        return port;
    }
    public Broker getBroker() {
        return broker;
    }
}
