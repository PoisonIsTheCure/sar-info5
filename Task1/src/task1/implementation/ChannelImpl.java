package task1.implementation;

import task1.specification.Channel;


public class ChannelImpl extends Channel {

    private BufferController receptionBuffer;
    private BufferController emissionBuffer;
    private Rdv rdv;
    private boolean disconnected = false;        // Indicates fully disconnected state
    private boolean halfDisconnected = false;    // Indicates that disconnection has been initiated but pending bytes are left

    public ChannelImpl(BufferController receptionBuffer, BufferController emissionBuffer, Rdv rdv) {
        this.receptionBuffer = receptionBuffer;
        this.emissionBuffer = emissionBuffer;
        this.rdv = rdv;
    }

    @Override
    public synchronized int write(byte[] bytes, int offset, int length) {
        // Check if the channel is disconnected or half-disconnected
        if (this.disconnected) {
            throw new IllegalStateException("Cannot write to a disconnected channel");
        } else if (this.halfDisconnected) {
            throw new IllegalStateException("The channel is half-disconnected (other end not reading) and cannot be written to");
        }

        int bytesWritten = 0;

        // Write bytes one at a time, blocking if necessary
        for (int i = offset; i < offset + length; i++) {
            if (this.disconnected) {
                throw new IllegalStateException("Channel is disconnected during write");
            }

            try {
                emissionBuffer.push(bytes[i]); // Push bytes to the emission buffer
                bytesWritten++;
            } catch (InterruptedException e) {
                if (bytesWritten == 0) {
                    return -1; // Return -1 if no bytes were written
                }
                return bytesWritten;
            }
        }

        return bytesWritten;
    }

    @Override
    public synchronized int read(byte[] bytes, int offset, int length) {
        // Check if the channel is fully disconnected
        if (this.disconnected) {
            throw new IllegalStateException("Cannot read from a fully disconnected channel");
        }

        int bytesRead = 0;

        // Read bytes one at a time, blocking if necessary
        for (int i = offset; i < offset + length; i++) {
            if (this.disconnected) {
                throw new IllegalStateException("Channel is disconnected during read");
            }

            try {
                bytes[bytesRead + offset] = receptionBuffer.pull(); // Pull bytes from the reception buffer
                bytesRead++;
            } catch (InterruptedException e) {
                if (bytesRead == 0) {
                    return -1; // Return -1 if no bytes were read
                }
                return bytesRead;
            }

            // Check if the buffer is empty and half-disconnected, complete disconnection
            if (halfDisconnected && receptionBuffer.empty()) {
                this.disconnected = true;
                this.halfDisconnected = false;
                throw new IllegalStateException("Channel is now fully disconnected after reading in-transit bytes");
            }
        }

        return bytesRead;
    }

    public void halfDisconnect() {
        if (!this.disconnected) {
            // only half-disconnect if not fully disconnected (local disconnection requested
            this.halfDisconnected = true;
        }
    }

    @Override
    public synchronized void disconnect() {
        // local disconnection requested
        this.disconnected = true;
        this.rdv.disconnect();
    }

    @Override
    public boolean disconnected() {
        return this.disconnected;
    }
}
