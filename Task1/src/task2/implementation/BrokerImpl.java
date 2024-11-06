package task2.implementation;

import task2.specification.Broker;
import task2.specification.Channel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BrokerImpl extends Broker {
    private BrokerManager manager;

    // Stores the Rdv objects per port
    private final Map<Integer, Rdv> rdvMap;

    public BrokerImpl(String name, BrokerManager manager) {
        super(name);
        this.manager = manager;
        this.manager.addBroker(this);
        this.rdvMap = new HashMap<>();
    }

    @Override
    public Channel accept(int port) throws IOException {
        if (port < 0) {
            throw new IOException("Invalid port number");
        }

        // Retrieve the Rdv of the port, or create a new one if it doesn't exist
        Rdv acceptRdv;
        synchronized (this.rdvMap)  {
            acceptRdv = rdvMap.computeIfAbsent(port, k -> new Rdv(this));
        }

        // Notify the Rdv that the connection has been accepted
        return acceptRdv.waitForConnect();
    }

    @Override
    public Channel connect(String name, int port) throws IOException {
        // First we need to check if the broker is available
        BrokerImpl broker = (BrokerImpl) this.manager.getBroker(name);

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

        Rdv connectRdv;

        synchronized (this.rdvMap) {
            connectRdv = rdvMap.computeIfAbsent(port, k -> new Rdv(this));
        }

        // Allow Channel Creation by sending the Sender Broker Instance
        connectRdv.setConnectBroker(senderBroker);

        return connectRdv;
    }

    // Remove Rdv from the queue
    public void removeRdvAndDisconnect(Rdv rdv) {
        synchronized (this.rdvMap) {
            for (Map.Entry<Integer, Rdv> entry : rdvMap.entrySet()) {
                if (entry.getValue() == rdv) {
                    rdvMap.remove(entry.getKey());
                    break;
                }
            }
        }
    }
}
