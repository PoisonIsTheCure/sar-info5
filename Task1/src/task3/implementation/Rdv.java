package task3.implementation;

import task3.CircularBuffer;
import task3.specification.Broker;
import task3.specification.Channel;

import java.util.concurrent.Semaphore;

public class Rdv {

    // Circular Buffer static Size
    public static final int BUFFER_SIZE = 10;

    // Semaphores to control the connection
    private Semaphore waitingControlSemaphore;

    // Buffers
    private CircularBuffer receptionBuffer;
    private CircularBuffer emissionBuffer;

    private Broker connectBroker;
    private Broker acceptBroker;

    private ChannelImpl acceptChannel;
    private ChannelImpl connectChannel;

    private boolean channelsEstablished = false;

    public Rdv(Broker acceptBroker) {
        this.acceptBroker = acceptBroker;
        this.waitingControlSemaphore = new Semaphore(1);
    }

    public synchronized void setConnectBroker(Broker connectBroker) {
        this.connectBroker = connectBroker;
        this.channelsEstablished = true;
        establishChannels();
    }

    public Channel waitForAccept() {
        // Called by the broker that requested the connection

        waitForOtherBroker();

        return connectChannel;
    }

    /**
     * Wait for the connection to be Accepted
     *
     * Broker that Accept is the only one that can call this method
     *
     * @throws IllegalStateException when the connection is not accepted
     */
    public Channel waitForConnect() throws IllegalStateException {
        // Called by the broker that accepted the connection

        waitForOtherBroker();

        return acceptChannel;
    }


    private void establishChannels() {
        int capacity = BUFFER_SIZE;
        this.receptionBuffer = new CircularBuffer(capacity);
        this.emissionBuffer = new CircularBuffer(capacity);

        this.acceptChannel = new ChannelImpl(receptionBuffer, emissionBuffer, this);
        this.connectChannel = new ChannelImpl(emissionBuffer, receptionBuffer, this);

    }

    public void disconnect() {
        // reset the channels to delete the Rdv
        this.channelsEstablished = false;

        // Notify the broker that the connection has been closed
        this.acceptChannel.halfDisconnect();
        this.connectChannel.halfDisconnect();

        // Notify the broker that accepted the connection to remove the Rdv from the list
        ((BrokerImpl) acceptBroker).removeRdvAndDisconnect(this);
    }

    public String getAcceptBrokerName() {
        return acceptBroker.getName();
    }

    public String getConnectBrokerName() {
        return connectBroker.getName();
    }

    private void waitForOtherBroker() {
        try {
            if (waitingControlSemaphore.tryAcquire()){
                waitingControlSemaphore.acquire();
            }else {
                waitingControlSemaphore.release();
                waitingControlSemaphore.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
