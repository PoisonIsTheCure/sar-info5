package task3.implementation;

import task3.specification.*;

import java.io.IOException;
import java.util.HashMap;

public class BindEvent implements Event {

    // Lists of currently active acceptors
    static final HashMap<String, HashMap<Integer, AcceptWaiter>> acceptors = new HashMap<>();

    private int port;
    private QueueBroker.AcceptListener listener;
    private Broker broker;
    public BindEvent(Broker broker,int port, QueueBroker.AcceptListener listener) {
        this.broker = broker;
        this.port = port;
        this.listener = listener;
    }

    class AcceptWaiter implements Runnable {
        private final int port;
        private final Broker broker;
        private final QueueBroker.AcceptListener listener;
        private boolean running = true;
        public AcceptWaiter(Broker broker,int port, QueueBroker.AcceptListener listener) {
            this.broker = broker;
            this.port = port;
            this.listener = listener;
        }
        @Override
        public void run() {
            while (running) {
                try {
                    Channel channel = broker.accept(port);
                    MessageQueue messageQueue = new MessageQueueImpl(channel);
                    listener.accepted(messageQueue);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        /**
         * Stop the thread (called by UnBindEvent)
         */
        public void stop() {
            this.running = false;
            Thread.currentThread().interrupt();
        }
    }


    // BindEvent react function Implementation
    @Override
    public void react() {
        if (!acceptors.containsKey(broker.getName())) {
            acceptors.put(broker.getName(), new HashMap<>());
        }
        if (acceptors.get(broker.getName()).containsKey(port)) {
            throw new RuntimeException("Port already bound");
        }
        AcceptWaiter acceptWaiter = new AcceptWaiter(broker, port, listener);
        acceptors.get(broker.getName()).put(port, acceptWaiter);
        new TaskImpl(acceptWaiter).start();
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return broker.getName();
    }
}
