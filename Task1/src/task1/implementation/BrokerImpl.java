package task1.implementation;

import task1.specification.Broker;
import task1.specification.Channel;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class BrokerImpl extends Broker {
    private BrokerManager manager;

    // Stores the queues of Rdv objects per port
    private final Map<Integer, Queue<Rdv>> rdvMap;

    public BrokerImpl(String name, BrokerManager manager) {
        super(name);
        this.manager = manager;
        this.manager.addBroker(this);
        this.rdvMap = new HashMap<>();
    }

    @Override
    public synchronized Channel accept(int port) throws IOException {
        if (port < 0) {
            throw new IOException("Invalid port number");
        }

        // Retrieve the queue for the port, or create a new one if it doesn't exist
        Queue<Rdv> rdvQueue;
        synchronized (this.rdvMap) {
            rdvQueue = rdvMap.get(port);
            if (rdvQueue == null) {
                rdvQueue = new LinkedList<>();
                rdvMap.put(port, rdvQueue);
            }

            if (rdvQueue.isEmpty()) {
                Rdv newRdv = new Rdv(this);
                rdvQueue.add(newRdv);
            }

            // Retrieve and remove the oldest Rdv from the queue
            Rdv oldestRdv = rdvQueue.poll();
            if (oldestRdv == null) {
                // In this case, we add the Rdv to the queue
                oldestRdv = new Rdv(this);
                rdvQueue.add(oldestRdv);
            }

            // Notify the Rdv that the connection has been accepted
            return oldestRdv.waitForConnect();
        }
    }

    @Override
    public Channel connect(String name, int port) throws IOException {
        // First we need to check if the broker is available
        BrokerImpl broker = this.manager.getBroker(name);

        if (broker == null) {
            return null;
        }

        // Send the connection request and wait for it to be accepted in the Rdv class
        Rdv rdv = broker.receiveConnectionRequest(this, port);
        return rdv.waitForAccept();
    }

    public void closeBroker() {
        this.manager.removeBroker(this);
    }

    public String getName() {
        return this.name;
    }

    // Internal function between brokers
    public Rdv receiveConnectionRequest(BrokerImpl senderBroker, int port) {
        // Create or retrieve the queue for the specified port
        Queue<Rdv> rdvQueue;
        Rdv newRdv;

        synchronized (this.rdvMap) {
            rdvQueue = rdvMap.get(port);
            if (rdvQueue == null) {
                rdvQueue = new LinkedList<>();
                rdvMap.put(port, rdvQueue);
            }

            // Check if a Rdv already exists for the sender broker
            for (Rdv rdv : rdvQueue) {
                if (rdv.getConnectBrokerName().equals(senderBroker.getName())) {
                    return rdv;
                }
            }

            // If not, create a new Rdv and add it to the queue
            newRdv = new Rdv(this);
            newRdv.setConnectBroker(senderBroker);
            rdvQueue.add(newRdv);
        }

        // Allow Channel Creation by sending the Sender Broker Instance
        newRdv.setConnectBroker(senderBroker);

        return newRdv;
    }

    // Remove Rdv from the queue
    public void removeRdvAndDisconnect(Rdv rdv) {
        synchronized (this.rdvMap) {
            for (Queue<Rdv> queue : rdvMap.values()) {
                synchronized (queue) {
                    queue.remove(rdv);
                }
            }
        }
    }
}
