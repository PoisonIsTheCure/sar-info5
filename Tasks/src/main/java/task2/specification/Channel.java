package task2.specification;

/**
 * This class represents a communication channel between two brokers
 * <p>
 * The channel is used to send and receive data (bytes) between brokers
 *
 * This channel garanties that the data will be sent in the same order it was sent, meaning that
 * it's a FIFO channel
 */
public abstract class Channel {


    /**
     * This function reads the given length from Channel.
     * <p>
     *
     * <i>
     *     Note: this function is blocking function, meaning that it could block
     *     the thread in case if given length not read yet
     * </i>.
     *
     * @param bytes byte array to store values into
     * @param offset offset where to start storing
     * @param length length of data to be read
     *
     * @return n number of bytes that have been read
     */
    public int read(byte[] bytes, int offset, int length){
        throw new IllegalStateException("Unimplemented Method");
    }

    /**
     * Sends a stream of bytes given an array a length of bytes to send and an offset
     *
     * Note : if length given is bigger than the array range, then only available range will
     * be sent
     *
     * @param bytes is the array of bytes
     * @param offset where to start sending
     * @param length length of data to send
     * @return Number of bytes sent successfully
     */
    public int write(byte[] bytes, int offset, int length){
        throw new IllegalStateException("Unimplemented Method");
    }

    /**
     * disconnect from current connection (if connected)
     *
     * <i>Note that if user requested to disconnect while reading/writing on
     * the channel, the channel will wait until the current job finish before disconnecting,
     * but if user check status it will appear as disconnected and no new jobs will be accepted
     *  (read and write requests will throw DisconnectedException)
     *  </i>
     */
    public void disconnect(){
        throw new IllegalStateException("Unimplemented Method");
    }

    /**
     * Send a boolean evaluating true if connection is disconnected, and false if
     * a connection is currently running
     * @return boolean disconnected or not
     */
    public boolean disconnected(){
        throw new IllegalStateException("Unimplemented Method");
    }
}
