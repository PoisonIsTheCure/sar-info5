package task4.specification;

/**
 * Abstract class representing a broker that manages message queues for communication.
 */
public abstract class QueueBroker {

    protected Broker broker;

    public interface AcceptListener {

        void accepted(MessageQueue queue);

    }

    public interface ConnectListener {
        void connected(MessageQueue messageQueue);
        void refused();
    }


    /**
     * Retrieves the name of this QueueBroker (Broker name)
     *
     * @return The name of the QueueBroker (Broker).
     */
    public abstract String name();


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
     * @param listener The listener to be notified when a connection is accepted.
     */
    public abstract void bind(int port, AcceptListener listener);


    /**
     * Unbinds a message queue from a specific port.
     *
     * @param port The port to unbind the message queue from.
     */
    public abstract void unbind(int port);
}