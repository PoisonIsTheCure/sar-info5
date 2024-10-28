package task4.implementation;

import task4.specification.Broker;
import task4.specification.QueueBroker;

import java.io.IOException;

public class BrokerImpl extends Broker {
    private BrokerManager manager;


    public BrokerImpl(String name) {
        super(name);
        this.manager = BrokerManager.getInstance();
        this.manager.addBroker(this);
    }

    @Override
    public void bind(int port, QueueBroker.AcceptListener listener) throws IOException, InterruptedException {
        // TODO: Implement this method
    }

    @Override
    public void connect(String name, int port, QueueBroker.ConnectListener listener) throws IOException, InterruptedException {
        // TODO: Implement this method
    }

    public void closeBroker() {
        this.manager.removeBroker(this);
    }

    public String getName() {
        return this.name;
    }

}
