package task1.specification;

public abstract class Task extends Thread {

    protected Broker broker;

    protected Runnable taskRunnable;

    public Task(Broker b, Runnable r){
        this.broker = b;
        this.taskRunnable = r;
    }

    /**
     * Send the Broker associated to the current Task
     * each task has its own broker.
     *
     * This function should be called by the task runnable
     *
     * @throws IllegalStateException if the method is called without an existing Task for
     * the current thread
     *
     * @return Broker corresponding to the current Task or <code>null</code> if the Task is not associated with a Broker
     */
    public static Broker getBroker() throws IllegalStateException {
        return ((Task) Thread.currentThread()).broker;
    }

}
