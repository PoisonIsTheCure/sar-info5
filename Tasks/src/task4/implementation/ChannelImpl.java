package task4.implementation;

import task4.CircularBuffer;
import task4.specification.Channel;
import task4.specification.DisconnectedException;

public class ChannelImpl extends Channel {

    private CircularBuffer receptionBuffer;
    private CircularBuffer emissionBuffer;
    private boolean disconnected = false;        // Indicates fully disconnected state
    private boolean halfDisconnected = false;    // Indicates that disconnection has been initiated but pending bytes are left

    public ChannelImpl(CircularBuffer receptionBuffer, CircularBuffer emissionBuffer) {
        this.receptionBuffer = receptionBuffer;
        this.emissionBuffer = emissionBuffer;
    }

    @Override
    public boolean write(byte[] bytes, int offset, int length) throws DisconnectedException {
        // TODO: Implement this method
        return false;
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
}