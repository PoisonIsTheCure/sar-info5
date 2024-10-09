package task3.implementation;

import task3.specification.*;

import java.io.IOException;

public class QueueBrokerImpl extends QueueBroker {

    private EventPump eventPump;

    public QueueBrokerImpl(String name) {
        super(new BrokerImpl(name));
        this.eventPump = EventPump.getInstance();
    }


    @Override
    public String name() {
        return this.broker.getName();
    }


//    @Override
//    public MessageQueue accept(int port) {
//        try {
//            // Use the broker to accept a connection and get a Channel
//            Channel channel = broker.accept(port);
//            if (channel == null) {
//                throw new IOException("Failed to accept connection on port " + port + " for " + name());
//            }
//
//            // Create a MessageQueue that uses the Channel
//            MessageQueue messageQueue = new MessageQueueImpl(channel);
//            return messageQueue;
//
//        } catch (IOException e) {
//            throw new IllegalStateException("Error accepting connection: " + e.getMessage());
//        }
//    }

//    @Override
//    public MessageQueue connect(String name, int port) {
//        try {
//            // Use the broker to connect to another broker's Channel
//            Channel channel = broker.connect(name, port);
//            if (channel == null) {
//                throw new IOException("Failed to connect to " + name + " on port " + port + " for " + name());
//            }
//
//            // Create a MessageQueue that uses the Channel
//            MessageQueue messageQueue = new MessageQueueImpl(channel);
//            return messageQueue;
//
//        } catch (IOException e) {
//            throw new IllegalStateException("Error connecting to broker: " + e.getMessage());
//        }
//
//    }


    @Override
    public boolean connect(String name, int port, ConnectListener listener) {
        ConnectEvent event = new ConnectEvent(name, port, listener);
        eventPump.post(event);
        return true;
    }

    @Override
    public void bind(int port, AcceptListener listener) {
        BindEvent event = new BindEvent(this.broker,port, listener);
        eventPump.post(event);
    }

    @Override
    public void unbind(int port) {
        return;
    }


}