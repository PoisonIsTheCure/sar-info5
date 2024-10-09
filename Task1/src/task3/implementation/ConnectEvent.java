package task3.implementation;

import task3.specification.*;

import java.io.IOException;

public class ConnectEvent implements Event {

    private String name;
    private int port;
    private QueueBroker.ConnectListener listener;

    public ConnectEvent(String name , int port, QueueBroker.ConnectListener listener) {
        this.name = name;
        this.port = port;
        this.listener = listener;
    }

    @Override
    public void react() {
        Broker broker = BrokerManager.getInstance().getBroker(name);
        if (broker == null) {
            listener.refused();
        } else {
            try {
                Channel channel = broker.connect(name, port);
                // Create a MessageQueue that uses the Channel
                MessageQueue messageQueue = new MessageQueueImpl(channel);
                listener.connected(messageQueue);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }
}
