package task3.implementation;

import task3.specification.Broker;
import task3.specification.QueueBroker;
import task3.specification.Task;

public class TaskImpl extends Task {

    public TaskImpl(Runnable r) {
        super(r);
    }

    public TaskImpl(Broker b, Runnable r) {
        super(b, r);
    }

    public TaskImpl(QueueBroker b, Runnable r) {
        super(b, r);
    }

    @Override
    public void run() {
        taskRunnable.run();
    }
}
