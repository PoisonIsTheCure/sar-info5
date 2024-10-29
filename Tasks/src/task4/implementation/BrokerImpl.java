package task4.implementation;

import org.tinylog.Logger;
import task4.CircularBuffer;
import task4.specification.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class BrokerImpl extends Broker {

    private final static int BUFFER_SIZE = 10;

    private final BrokerManager manager;

    private final HashMap<Integer, QueueBroker.AcceptListener> acceptListeners = new HashMap<>();

    private final HashMap<String,List<Integer>> connections = new HashMap<>();

    private final HashMap<Integer, HashMap<String, QueueBroker.ConnectListener>> waitingConnections = new HashMap<>();




    public BrokerImpl(String name) {
        super(name);
        this.manager = BrokerManager.getInstance();
        this.manager.addBroker(this);
    }

    /**
     * This function is called by the destination broker (who received the connection request)
     * The function establishes the connection between the two brokers, having the 2 listeners (Connect and Accept)
     *
     * The function is called in the EventPump thread (by AcceptEvent), so no concurrency issues
     * @param connectListener The listener of the source broker
     * @param acceptListener The listener of the destination broker
     * @return
     */
    private boolean establishConnection(QueueBroker.ConnectListener connectListener, QueueBroker.AcceptListener acceptListener) {

        // Create buffers
        CircularBuffer buffer1 = new CircularBuffer(BrokerImpl.BUFFER_SIZE);
        CircularBuffer buffer2 = new CircularBuffer(BrokerImpl.BUFFER_SIZE);

        // Create Channels
        Channel channel1 = new ChannelImpl(buffer1, buffer2);
        Channel channel2 = new ChannelImpl(buffer2, buffer1);

        // Create MessageQueues
        MessageQueue messageQueue1 = new MessageQueueImpl(channel1);
        MessageQueue messageQueue2 = new MessageQueueImpl(channel2);

        // Notify the listeners
        connectListener.connected(messageQueue1);
        acceptListener.accepted(messageQueue2);

        return true;
    }

    @Override
    public void bind(int port, QueueBroker.AcceptListener listener) throws IOException, InterruptedException {
        Task.task().post(new BindEvent(port, listener));
    }

    @Override
    public void unbind(int port) {
        Task.task().post(new UnBindEvent(port));
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
            Task.task().post(new AcceptEvent(port, acceptListener, listener));
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
        Task.task().post(new ConnectEvent(destinationBroker, port, listener));
    }

    // Inner class for AcceptEvent
    private class AcceptEvent implements Event {
        private final int port;
        private final QueueBroker.AcceptListener acceptListener;
        private final QueueBroker.ConnectListener connectListener;

        public AcceptEvent(int port, QueueBroker.AcceptListener acceptListener, QueueBroker.ConnectListener connectListener) {
            this.port = port;
            this.acceptListener = acceptListener;
            this.connectListener = connectListener;
        }

        @Override
        public void react() {
            // Establish the connection
            if (BrokerImpl.this.establishConnection(connectListener, acceptListener)) {
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
    private class ConnectEvent implements Event {
        private final BrokerImpl destinationBroker;
        private final int port;
        private final QueueBroker.ConnectListener listener;

        public ConnectEvent(BrokerImpl destinationBroker, int port, QueueBroker.ConnectListener listener) {
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

    private class BindEvent implements Event {

        private final int port;
        private final QueueBroker.AcceptListener listener;

        public BindEvent(int port, QueueBroker.AcceptListener listener) {
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

                    currentTask.post(new AcceptEvent(port, listener, connectListener));
                    listeners.remove(connectBrokerName);
                }
            }
        }
    }

    private class UnBindEvent implements Event {
        private final int port;

        public UnBindEvent(int port) {
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
