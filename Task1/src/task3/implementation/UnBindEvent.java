package task3.implementation;

import task3.specification.Broker;
import task3.specification.Event;
import task3.specification.MessageQueue;
import task3.specification.Task;
import task3.tests.MessageQueueTest;

public class UnBindEvent implements Event {
    private int port;
    private Broker broker;

    public UnBindEvent(Broker broker,int port) {
        this.broker = broker;
        this.port = port;
    }
    @Override
    public void react() {
        if (!BindEvent.acceptors.containsKey(broker.getName())) {
            throw new RuntimeException("Broker not bound to any port");
        }
        if (!BindEvent.acceptors.get(broker.getName()).containsKey(port)) {
            throw new RuntimeException("Port not bound");
        }
        Task waiter = BindEvent.acceptors.get(broker.getName()).get(port);
        waiter.interrupt();
        BindEvent.acceptors.get(broker.getName()).remove(port);
    }
    public int getPort() {
        return port;
    }
    public Broker getBroker() {
        return broker;
    }
}
