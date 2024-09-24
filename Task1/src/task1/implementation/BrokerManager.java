package task1.implementation;

import task1.specification.Broker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class BrokerManager {
    public List<BrokerImpl> brokers = new ArrayList<BrokerImpl>();

    public BrokerManager() {
    }

    public void addBroker(BrokerImpl broker) {
        brokers.add(broker);
    }

    public void removeBroker(BrokerImpl broker) {
        brokers.remove(broker);
    }

    public BrokerImpl getBroker(String name) {
        for (BrokerImpl broker : brokers) {
            if (broker.getName().equals(name)) {
                return (BrokerImpl) broker;
            }
        }
        
        return null;
    }


}
