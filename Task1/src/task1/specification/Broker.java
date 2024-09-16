package task1.specification;

import java.io.IOException;

public abstract class Broker {
    public Broker(String name) {
    }

    /**
     * A function that accept incoming connections, given the port number
     * It return the communication channel
     *
     * @throws IOException if an I/O error occurs when creating the socket.
     *
     * @param port Number of connection port
     * @return specification.Channel
     */
    public Channel accept(int port) throws IOException{
        throw new IllegalStateException("Unimplemented Method");
    }

    /**
     * Sends a connection request to the given port associated with the given broker
     * name
     *
     * @throws IOException if an I/O error occurs when creating the socket.
     *
     * @param name connection name (The Name of the Broker to connect with)
     * @param port connection port
     * @return The Communication Channel
     */
    public Channel connect(String name, int port) throws IOException{
        throw new IllegalStateException("Unimplemented Method");
    }
}


