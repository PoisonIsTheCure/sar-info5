package task1.implementation;

import task1.specification.Broker;
import task1.specification.Task;

public class TaskImpl extends Task {
    public TaskImpl(Broker b, Runnable r) {
        super(b, r);
    }

    @Override
    public void run() {
        taskRunnable.run();
    }
}
