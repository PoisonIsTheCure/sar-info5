package task3.specification;

/**
 * Abstract class representing a message queue used for sending and receiving messages.
 */
public abstract class MessageQueue {

    protected Listener listener;

    public interface Listener {

        void received(byte[] msg);

        void closed();
    }


    /**
     * Interface for the listener that will be notified when a message is received or the queue is closed.
     */
    public abstract void setListener(Listener l);


    /**
     * Sends the requested message to the message queue.
     *
     * This method doesn't block the caller.
     * It will send the message as soon as possible, meaning that the message may not be sent immediately, if
     * other messages are being sent from the queue.
     *
     * @param bytes The byte array containing the message to send.
     */
    public abstract void send(byte[] bytes) throws DisconnectedException;

    /**
     * Sends the requested message to the message queue.
     *
     * This method doesn't block the caller.
     * It will send the message as soon as possible, meaning that the message may not be sent immediately, if
     * other messages are being sent from the queue.
     *
     * @param bytes  The byte array containing the message to send.
     * @param offset The start offset in the byte array from which to begin sending.
     * @param length The number of bytes to send from the byte array.
     */
    public abstract void send(byte[] bytes, int offset, int length) throws DisconnectedException;

    /**
     * Closes the message queue, releasing any resources associated with it.
     * On calling this method, the message queue should no longer be used on the caller side.
     * Any further operations on the message queue after calling close() should throw an exception.
     *
     * On the callee side, the user will be able to read messages that were sent before the queue was closed.
     * But callee will not be able to send any more messages.
     */
    public abstract void close();

    /**
     * Checks whether the message queue is closed.
     *
     * This method can be used to check if the message queue is still open for sending and receiving messages.
     * If the queue is closed locally, the method will return true.
     * If the queue is closed remotely, the method will return true only after all remaining messages have been read.
     *
     * Although the queue was closed remotely, this method might return false if there are still messages to be read,
     * but any write operation will throw a DisconnectedException.
     *
     * @return true if the queue is fully closed, false otherwise.
     */
    public abstract boolean closed();

    /**
     * Get the listener
     */
    public abstract MessageQueue.Listener getListener();
}