package task3.implementation;

import task3.specification.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class BindEvent implements Event {

    // Lists of currently active acceptors
    static final ConcurrentHashMap<String, HashMap<Integer, Task>> acceptors = new ConcurrentHashMap<>();

    private int port;
    private QueueBroker.AcceptListener listener;
    private Broker broker;
    public BindEvent(Broker broker,int port, QueueBroker.AcceptListener listener) {
        this.broker = broker;
        this.port = port;
        this.listener = listener;
    }

    static class AcceptWaiter implements Runnable {
        private final int port;
        private final Broker broker;
        private final QueueBroker.AcceptListener listener;

        public AcceptWaiter(Broker broker,int port, QueueBroker.AcceptListener listener) {
            this.broker = broker;
            this.port = port;
            this.listener = listener;
        }
        @Override
        public void run() {
            while (true) {
                Channel channel = null;
                try {
                    channel = broker.accept(port);
                    MessageQueue messageQueue = new MessageQueueImpl(channel);
                    listener.accepted(messageQueue);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Preserve interrupt status
                    break; // Exit the loop
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
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
        Task task = new TaskImpl(broker, acceptWaiter);
        acceptors.get(broker.getName()).put(port, task);
        task.start();
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return broker.getName();
    }
}
