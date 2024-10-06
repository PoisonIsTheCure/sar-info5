package task2.implementation;

import task2.specification.Broker;
import task2.specification.MessageQueue;
import task2.specification.QueueBroker;
import task2.specification.Channel;

import java.io.IOException;

public class QueueBrokerImpl extends QueueBroker {

    public QueueBrokerImpl(Broker broker) {
        super(broker);
    }

    @Override
    public String name() {
        return this.broker.getName();
    }

    @Override
    public MessageQueue accept(int port) {
        try {
            // Use the broker to accept a connection and get a Channel
            Channel channel = broker.accept(port);
            if (channel == null) {
                throw new IOException("Failed to accept connection on port " + port + " for " + name());
            }

            // Create a MessageQueue that uses the Channel
            MessageQueue messageQueue = new MessageQueueImpl(channel);
            return messageQueue;

        } catch (IOException e) {
            throw new IllegalStateException("Error accepting connection: " + e.getMessage());
        }
    }

    @Override
    public MessageQueue connect(String name, int port) {
        try {
            // Use the broker to connect to another broker's Channel
            Channel channel = broker.connect(name, port);
            if (channel == null) {
                throw new IOException("Failed to connect to " + name + " on port " + port + " for " + name());
            }

            // Create a MessageQueue that uses the Channel
            MessageQueue messageQueue = new MessageQueueImpl(channel);
            return messageQueue;

        } catch (IOException e) {
            throw new IllegalStateException("Error connecting to broker: " + e.getMessage());
        }

    }
}