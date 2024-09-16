package task1.specification;

public abstract class Task extends Thread {
    Task(Broker b, Runnable r){

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
     * @return Broker corresponding to the current Task
     */
    public static Broker getBroker() throws IllegalStateException {
        throw new IllegalStateException("Unimplemented Method");
    }

}
