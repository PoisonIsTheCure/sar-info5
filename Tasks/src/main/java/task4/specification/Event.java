package task4.specification;

public abstract class Event {
    private Task parentTask;

    public Event(Task parentTask){
        this.parentTask = parentTask;
    }

    /**
     * This method is called when the event is triggered.
     * It means that the event is ready to be processed.
     *
     * This Method should not be blocking.
     *
     * If this method is blocking, it will block the EventPump and the other events will not be processed.
     * Any Threaded Mix should be handled by the Event itself.
     */
    public abstract void react();

    public Task getParentTask(){
        return parentTask;
    }
}
