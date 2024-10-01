package task1.implementation;

import task1.specification.QueueBroker;
import java.util.HashMap;

/**
 * Manager class for handling a collection of QueueBroker instances.
 */
public class QueueBrokerManager {

    // HashMap to store QueueBroker instances, where the key is the broker's name.
    private final HashMap<String, QueueBroker> queueBrokers = new HashMap<>();

    /**
     * Constructor for QueueBrokerManager.
     */
    public QueueBrokerManager() {}

    /**
     * Adds a QueueBroker to the manager.
     *
     * @param queueBroker The QueueBroker instance to add.
     * @throws IllegalArgumentException if a QueueBroker with the same name already exists.
     */
    public synchronized void addQueueBroker(QueueBroker queueBroker) {
        String name = queueBroker.name();
        QueueBroker existingBroker = queueBrokers.get(name);

        if (existingBroker != null) {
            throw new IllegalArgumentException("QueueBroker " + name + " already exists");
        }

        queueBrokers.put(name, queueBroker);
    }

    /**
     * Removes a QueueBroker from the manager.
     *
     * @param queueBroker The QueueBroker instance to remove.
     */
    public synchronized void removeQueueBroker(QueueBroker queueBroker) {
        queueBrokers.remove(queueBroker.name());
    }

    /**
     * Retrieves a QueueBroker by its name.
     *
     * @param name The name of the QueueBroker to retrieve.
     * @return The QueueBroker instance, or null if not found.
     */
    public QueueBroker getQueueBroker(String name) {
        return queueBrokers.get(name);
    }

    /**
     * Retrieves all managed QueueBrokers.
     *
     * @return A collection of all QueueBrokers.
     */
    public synchronized HashMap<String, QueueBroker> getAllQueueBrokers() {
        return new HashMap<>(queueBrokers);
    }
}