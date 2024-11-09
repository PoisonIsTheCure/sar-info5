package task1.implementation;

import task1.CircularBuffer;
import task1.specification.Channel;
import task1.specification.DisconnectedException;

public class ChannelImpl extends Channel {

    private CircularBuffer receptionBuffer;
    private CircularBuffer emissionBuffer;
    private Rdv rdv;
    private boolean disconnected = false;        // Indicates fully disconnected state
    private boolean halfDisconnected = false;    // Indicates that disconnection has been initiated but pending bytes are left

    public ChannelImpl(CircularBuffer receptionBuffer, CircularBuffer emissionBuffer, Rdv rdv) {
        this.receptionBuffer = receptionBuffer;
        this.emissionBuffer = emissionBuffer;
        this.rdv = rdv;
    }

    @Override
    public int write(byte[] bytes, int offset, int length) throws DisconnectedException {
        // Check if the channel is disconnected or half-disconnected
        synchronized (emissionBuffer) {
            if (this.disconnected) {
                throw new DisconnectedException("Cannot write to a disconnected channel");
            } else if (this.halfDisconnected) {
                throw new DisconnectedException("The channel is half-disconnected (other end not reading) and cannot be written to");
            }
        }

        int bytesWritten = 0;

        // Write bytes one at a time, blocking if necessary
        for (int i = offset; i < offset + length; i++) {
            synchronized (emissionBuffer) {
                while (this.emissionBuffer.full()) {
                    try {
                        emissionBuffer.wait(); // Wait until space is available
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                this.emissionBuffer.push(bytes[i]);
                bytesWritten++;
                emissionBuffer.notifyAll(); // Notify any waiting threads
            }

            if (this.disconnected) {
                throw new DisconnectedException("Channel is disconnected during write");
            }
        }

        return bytesWritten;
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws DisconnectedException {
        // Check if the channel is fully disconnected
        synchronized (receptionBuffer) {
            if (this.disconnected) {
                throw new DisconnectedException("Cannot read from a fully disconnected channel");
            }
        }

        int bytesRead = 0;

        // Read bytes one at a time, blocking if necessary
        for (int i = offset; i < offset + length; i++) {
            synchronized (receptionBuffer) {
                while (this.receptionBuffer.empty()) {
                    if (halfDisconnected) {
                        this.disconnected = true;
                        this.halfDisconnected = false;
                        throw new DisconnectedException("Channel is now fully disconnected after reading in-transit bytes");
                    }
                    try {
                        receptionBuffer.wait(); // Wait until data is available
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                bytes[bytesRead + offset] = this.receptionBuffer.pull();
                bytesRead++;
                receptionBuffer.notifyAll(); // Notify any waiting threads
            }

            if (this.disconnected) {
                throw new DisconnectedException("Channel is disconnected during read");
            }
        }

        return bytesRead;
    }

    public void halfDisconnect() {
        synchronized (this) {
            if (!this.disconnected) {
                // only half-disconnect if not fully disconnected
                this.halfDisconnected = true;
            }
        }
    }

    @Override
    public void disconnect() {
        synchronized (this) {
            this.disconnected = true;
            this.rdv.disconnect();
        }
    }

    @Override
    public boolean disconnected() {
        return this.disconnected;
    }
}