package task3.specification;

public abstract class EventPump extends Thread {

    private static EventPump eventPumpInstance = null;

    private EventPump() {
        EventPump.eventPumpInstance = this;
    }

    public abstract EventPump getInstance();

    /**
     * This method allows EventPump users to post events to the EventPump.
     *
     * The Event will be waiting in the queue until given to the Executor.
     *
     * @param event The event to be posted.
     */
    public abstract void post(Event event);

    /**
     * This method starts the EventPump.
     *
     * So it start processing the events in the queue.
     */
    public abstract void start();

    /**
     * This method stops and kill the EventPump.
     *
     */
    public abstract void kill();
}
