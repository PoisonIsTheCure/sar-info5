package task4.implementation;

import task4.CircularBuffer;
import task4.specification.Channel;
import task4.specification.DisconnectedException;
import task4.specification.Event;
import task4.specification.Task;

public class ChannelImpl extends Channel {

    private CircularBuffer receptionBuffer;
    private CircularBuffer emissionBuffer;
    private boolean disconnected = false;        // Indicates fully disconnected state
    private boolean halfDisconnected = false;    // Indicates that disconnection has been initiated but pending bytes are left

    private boolean writePending = false;        // Indicates that a write operation is pending
    private boolean readPending = false;         // Indicates that a read operation is pending

    private ChannelReadListener readListener;

    public ChannelImpl(CircularBuffer receptionBuffer, CircularBuffer emissionBuffer) {
        this.receptionBuffer = receptionBuffer;
        this.emissionBuffer = emissionBuffer;

        // Set the read listener for the reception buffer
        receptionBuffer.setReadListener(new CircularBuffer.ReadListener() {
            @Override
            public void readDataAvailable() {
                if (readListener != null) {
                    readListener.readDataAvailable();
                }
            }
        });
    }

    @Override
    public int write(byte[] bytes, int offset, int length) throws DisconnectedException {
        // TODO: Implement this method
        return 0;
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws DisconnectedException {
        // TODO: Implement this method
        return 0;
    }

    public void halfDisconnect() {
        // TODO: Implement this method
    }

    @Override
    public void disconnect() {
        // TODO: Implement this method
    }

    @Override
    public boolean disconnected() {
        return this.disconnected;
    }

    @Override
    public void setChannelReadListener(ChannelReadListener listener) {
        this.readListener = listener;
    }


    /**
     * THE FOLLOWING CLASSES ARE EVENTS AND LISTENERS OWNED BY THE CHANNEL
     */

    private class ReadEvent implements Event {

        @Override
        public void react() {
            // TODO: Implement this method
        }
    }

    private class ChannelDisconnectEvent implements Event {

        @Override
        public void react() {
            // TODO: Implement this method
        }
    }

}