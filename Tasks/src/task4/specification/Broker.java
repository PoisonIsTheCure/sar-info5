package task4.specification;

import java.io.IOException;

public abstract class Broker {

    protected String name;

    public Broker(String name) {
        this.name = name;
    }

    /**
     * A function that accept incoming connections, given the port number
     *
     * The AcceptListener is used to notify the caller when the connection is established
     *
     * @throws IOException if an I/O error occurs when creating the socket.
     *
     * @param port Number of connection port
     * @return specification.Channel
     */
    public void bind(int port, QueueBroker.AcceptListener listener) throws IOException, InterruptedException {
        throw new IllegalStateException("Unimplemented Method");
    }

    /**
     * Sends a connection request to the given port associated with the given broker
     * The ConnectListener is used to notify the caller when the connection is established
     *
     * @throws IOException if an I/O error occurs when creating the socket.
     *
     * @param name connection name (The Name of the Broker to connect with)
     * @param port connection port
     * @return The Communication Channel
     */
    public void connect(String name, int port, QueueBroker.ConnectListener listener) throws IOException, InterruptedException {
        throw new IllegalStateException("Unimplemented Method");
    }

    /**
     * Unbinds a port from the broker.
     *
     * @param port The port to unbind the message queue from.
     */
    public void unbind(int port) {
        throw new IllegalStateException("Unimplemented Method");
    }

    /**
     * Get the name of the broker
     *
     * @return The name of the broker
     */
    public String getName() {
        return name;
    }

}
