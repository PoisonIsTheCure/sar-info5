package task3.specification;

/**
 * Abstract class representing a broker that manages message queues for communication.
 */
public abstract class QueueBroker {

    protected Broker broker;

    interface AcceptListener {

        void accepted(MessageQueue queue);

    }

    interface ConnectListener {
        void connected(MessageQueue messageQueue);
        void refused();
    }

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
     * Connects to a broker on a specified port and returns a MessageQueue.
     *
     * This method is non-blocking and will return immediately.
     * The MessageQueue will be set up to handle the connection once it is established.
     *
     * @param name The name of the broker to connect to.
     * @param port The port on which to connect.
     * @return The MessageQueue that is set up to handle the connection.
     */
    public abstract boolean connect(String name, int port, ConnectListener listener);


    /**
     * Binds a message queue to a specific port.
     *
     * @param port The port to bind the message queue to.
     * @param name The name of the message queue to bind.
     */
    public abstract void bind(int port, String name);


    /**
     * Unbinds a message queue from a specific port.
     *
     * @param port The port to unbind the message queue from.
     */
    public abstract void unbind(int port);
}