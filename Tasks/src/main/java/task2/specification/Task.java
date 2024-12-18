package task2.specification;

public abstract class Task extends Thread {

    protected Broker broker;
    protected QueueBroker queueBroker;

    protected Runnable taskRunnable;

    public Task(Broker b, Runnable r){
        this.broker = b;
        this.taskRunnable = r;
    }

    public Task(QueueBroker qb, Runnable r){
        this.queueBroker = qb;
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

    /**
     * Send the QueueBroker associated to the current Task
     * each task has its own QueueBroker.
     *
     * This function should be called by the task runnable
     *
     * @throws IllegalStateException if the method is called without an existing Task for
     * the current thread
     *
     * @return QueueBroker corresponding to the current Task, or <code>null</code> if the Task is not associated with a QueueBroker
     */
    public static QueueBroker getQueueBroker() throws IllegalStateException {
        return ((Task) Thread.currentThread()).queueBroker;
    }

}
