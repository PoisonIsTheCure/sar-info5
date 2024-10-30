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

    // Rdv
    private Rdv rdv;


    public ChannelImpl(Rdv rdv, CircularBuffer receptionBuffer, CircularBuffer emissionBuffer) {
        this.receptionBuffer = receptionBuffer;
        this.emissionBuffer = emissionBuffer;
        this.rdv = rdv;
    }

    @Override
    public int write(byte[] bytes, int offset, int length) throws DisconnectedException {
        if (disconnected || halfDisconnected) {
            throw new DisconnectedException("Channel is disconnected cannot write");
        }


        int bytesWritten = 0;
        while (!emissionBuffer.full() && length > 0) {
            if (disconnected || halfDisconnected) {
                throw new DisconnectedException("Channel Disconnected while writing");
            }
            emissionBuffer.push(bytes[offset]);
            offset++;
            length--;
            bytesWritten++;
        }
        return bytesWritten;
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws DisconnectedException {
        if (disconnected) {
            throw new DisconnectedException("Channel is disconnected cannot read");
        }
        if (halfDisconnected && receptionBuffer.empty()) {
            this.disconnect();
        }
        int bytesRead = 0;
        while (!receptionBuffer.empty() && length > 0) {
            bytes[offset] = receptionBuffer.pull();
            offset++;
            length--;
            bytesRead++;
        }
        return bytesRead;
    }

    @Override
    public void disconnect() {
        if (disconnected) {
            return;
        }
        else if (halfDisconnected) {
            disconnected = true;
            return;
        }else {
            disconnected = true;
            Task.task().post(new ChannelDisconnectEvent());
        }
    }

    private void halfDisconnect() {
        halfDisconnected = true;
    }

    @Override
    public boolean disconnected() {
        return this.disconnected;
    }

    /**
     * THE FOLLOWING CLASSES ARE EVENTS AND LISTENERS OWNED BY THE CHANNEL
     */

    private class ChannelDisconnectEvent implements Event {

        @Override
        public void react() {
            ChannelImpl otherChannel = rdv.getOtherChannel(ChannelImpl.this);
            if (otherChannel != null) {
                otherChannel.halfDisconnect();
            }
        }
    }

}