package task2.implementation;

import task2.specification.Broker;
import task2.specification.QueueBroker;
import task2.specification.Task;

public class TaskImpl extends Task {
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
