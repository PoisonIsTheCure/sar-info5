package task3.specification;

public abstract class EventPump extends Thread {

    protected static EventPump eventPumpInstance = null;

    protected EventPump() {
        EventPump.eventPumpInstance = this;
    }

    public static EventPump getInstance(){
        return eventPumpInstance;
    }

    public static void setInstance(EventPump eventPumpInstance){
        EventPump.eventPumpInstance = eventPumpInstance;
    }

    /**
     * This method allows EventPump users to post events to the EventPump.
     *
     * The Event will be waiting in the queue until given to the Executor.
     *
     * @param event The event to be posted.
     */
    public abstract void post(Event event);


    /**
     * This method stops and kill the EventPump.
     *
     */
    public abstract void kill();
}
