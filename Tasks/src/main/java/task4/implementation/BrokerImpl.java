package task4.implementation;

import org.tinylog.Logger;
import task4.specification.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class BrokerImpl extends Broker {

    private final BrokerManager manager;

    private final HashMap<Integer, QueueBroker.AcceptListener> acceptListeners = new HashMap<>();

    private final HashMap<String,List<Integer>> connections = new HashMap<>();

    private final HashMap<Integer, HashMap<String, QueueBroker.ConnectListener>> waitingConnections = new HashMap<>();




    public BrokerImpl(String name) {
        super(name);
        this.manager = BrokerManager.getInstance();
        this.manager.addBroker(this);
    }

    @Override
    public void bind(int port, QueueBroker.AcceptListener listener) throws IOException, InterruptedException {
        Task.task().post(new BindEvent(Task.task() ,port, listener));
    }

    @Override
    public void unbind(int port) {
        Task.task().post(new UnBindEvent(Task.task(),port));
    }

    /**
     * Add a connection to the registry of the broker
     * This method is called by the source broker
     * This method is called only in EventPump thread, so no concurrency issues
     * @param sourceBrokerName The name of the destination broker
     * @param port The port
     * @param listener
     */
    private void addConnection(String sourceBrokerName, int port, QueueBroker.ConnectListener listener) {
        // Check if a connection already exists
        if (this.connections.containsKey(sourceBrokerName)) {
            List<Integer> ports = this.connections.get(sourceBrokerName);
            if (ports.contains(port)) {
                Logger.info("Connection already exists in the registry of the broker"+ this.name);
                listener.refused();
                return;
            }
        }

        // Check if the port is Accepting connections
        if (this.acceptListeners.containsKey(port)) {
            // Get the AcceptListener
            QueueBroker.AcceptListener acceptListener = this.acceptListeners.get(port);

            // Create an AcceptEvent
            Task.task().post(new AcceptEvent(Task.task(), port, acceptListener, listener));
            return;
        }

        // Add the listener to the waitingConnections
        if (!this.waitingConnections.containsKey(port)) {
            this.waitingConnections.put(port, new HashMap<>());
        }
        this.waitingConnections.get(port).put(sourceBrokerName, listener);

        // Add the connection to the registry
        if (!this.connections.containsKey(sourceBrokerName)) {
            this.connections.put(sourceBrokerName, new LinkedList<>());
        }
        this.connections.get(sourceBrokerName).add(port);
    }

    @Override
    public void connect(String name, int port, QueueBroker.ConnectListener listener) throws IOException, InterruptedException {
        // Find the broker with the name
        BrokerImpl destinationBroker = (BrokerImpl) this.manager.getBroker(name);

        // Check if the destination broker doesn't exist
        if (destinationBroker == null) {
            Logger.error("Broker not found");
            throw new IOException("Broker not found");
        }

        // Check if the destination broker is the same as the current broker
        if (destinationBroker == this) {
            Logger.error("Cannot connect to self");
            throw new IOException("Cannot connect to self");
        }

        // Deposit the connection request at the destination broker
        Task.task().post(new ConnectEvent(Task.task(),destinationBroker, port, listener));
    }

    // Inner class for AcceptEvent
    private class AcceptEvent extends Event {
        private final int port;
        private final QueueBroker.AcceptListener acceptListener;
        private final QueueBroker.ConnectListener connectListener;

        public AcceptEvent(Task parentTask, int port, QueueBroker.AcceptListener acceptListener, QueueBroker.ConnectListener connectListener) {
            super(parentTask);
            this.port = port;
            this.acceptListener = acceptListener;
            this.connectListener = connectListener;
        }

        @Override
        public void react() {
            // Create rdv
            Rdv rdv = new Rdv();
            // Establish the connection
            if (rdv.createRdv(connectListener, acceptListener)) {
                // Remove the listener from the waitingConnections
                Logger.info("Connection established on broker " + BrokerImpl.this.name + " and port " + port);
            }
            else {
                // Notify the listener that the connection was refused
                connectListener.refused();
                Logger.error("Connection refused on broker " + BrokerImpl.this.name + " and port " + port);
            }
        }
    }

    // Inner class for ConnectEvent
    private class ConnectEvent extends Event {
        private final BrokerImpl destinationBroker;
        private final int port;
        private final QueueBroker.ConnectListener listener;

        public ConnectEvent(Task parentTask, BrokerImpl destinationBroker, int port, QueueBroker.ConnectListener listener) {
            super(parentTask);
            this.destinationBroker = destinationBroker;
            this.port = port;
            this.listener = listener;
        }

        @Override
        public void react() {
            try {
                destinationBroker.addConnection(getName(), port, listener);
            } catch (Exception e) {
                Logger.error("Failed to connect to broker: " + destinationBroker.getName() + " on port " + port, e);
            }
        }
    }

    /*
    An instance of InnerClass can exist only within an instance of OuterClass
    and has direct access to the methods and fields of its enclosing instance.

    cf. JavaDoc

    This is important for this implementation, because this what allows
    BindEvent to access the fields of BrokerImpl
     */

    private class BindEvent extends Event {

        private final int port;
        private final QueueBroker.AcceptListener listener;

        public BindEvent(Task parentTask, int port, QueueBroker.AcceptListener listener) {
            super(parentTask);
            this.port = port;
            this.listener = listener;
        }


        // BindEvent react function Implementation
        @Override
        public void react() {
            if (BrokerImpl.this.acceptListeners.containsKey(port)) {
                Logger.error("Port already in use");
                return;
            }
            BrokerImpl.this.acceptListeners.put(port, listener);


            // Check if there are waiting connections and accept them
            if (BrokerImpl.this.waitingConnections.containsKey(port)) {

                // Get HashMap of listeners
                HashMap<String, QueueBroker.ConnectListener> listeners = BrokerImpl.this.waitingConnections.get(port);

                // Get all the broker names waiting for a connection
                List<String> names = new LinkedList<>(listeners.keySet());

                Task currentTask = Task.task();

                // Iterate over all the broker names
                for (String connectBrokerName : names) {
                    // Get the listener for the broker
                    QueueBroker.ConnectListener connectListener = listeners.get(name);

                    currentTask.post(new AcceptEvent(Task.task(), port, listener, connectListener));
                    listeners.remove(connectBrokerName);
                }
            }
        }
    }

    private class UnBindEvent extends Event {
        private final int port;

        public UnBindEvent(Task parentTask,int port) {
            super(parentTask);
            this.port = port;
        }

        @Override
        public void react() {
            if (BrokerImpl.this.acceptListeners.containsKey(port)) {
                BrokerImpl.this.acceptListeners.remove(port);
                Logger.info("Port " + port + " unbound" + " on broker " + BrokerImpl.this.name);
            }
        }

    }
}
