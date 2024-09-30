package task1.implementation;

import task1.specification.Broker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class BrokerManager {
    public HashMap<String,Broker> brokers = new HashMap<String,Broker>();

    public BrokerManager() {
    }

    public synchronized void addBroker(BrokerImpl broker) {
        String name = broker.getName();
        Broker exists = brokers.get(name);

        if (exists != null) {
            throw new IllegalArgumentException("Broker " + name + " already exists");
        }

        brokers.put(name, broker);
    }

    public synchronized void removeBroker(BrokerImpl broker) {
        brokers.remove(broker);
    }

    public Broker getBroker(String name) {
        return brokers.get(name);
    }


}
