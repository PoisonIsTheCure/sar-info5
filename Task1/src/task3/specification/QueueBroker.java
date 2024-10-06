package task3.specification;

/**
 * Abstract class representing a broker that manages message queues for communication.
 */
public abstract class QueueBroker implements AcceptListener, ConnectListener {

    protected Broker broker;

    /**
     * Constructor to initialize the QueueBroker with a Broker.
     *
     * @param broker The Broker instance to be associated with this QueueBroker.
     */
    public QueueBroker(Broker broker) {
        this.broker = broker;
    }

    /**
     * Retrieves the name of this QueueBroker (Broker name)
     *
     * @return The name of the QueueBroker (Broker).
     */
    public abstract String name();

    /**
     * Accepts connections on a specified port and returns a MessageQueue.
     *
     * This method is blocking and will wait until a connection is established.
     *
     * @param port The port on which to accept connections.
     * @return The MessageQueue that is set up to handle incoming connections.
     */
    public abstract MessageQueue accept(int port);

    /**
     * Connects to a message queue identified by its name on a specific port.
     *
     *
     * @param name The name of the queue to connect to.
     * @param port The port on which the queue is available.
     * @return The MessageQueue representing the connection. or null if the corresponding queue is not found.
     */
    public abstract MessageQueue connect(String name, int port);
}