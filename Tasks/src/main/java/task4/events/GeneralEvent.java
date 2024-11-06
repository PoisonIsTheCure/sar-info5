package task4.events;

import task4.specification.Event;
import task4.specification.Task;

import java.util.UUID;

public class GeneralEvent extends Event {

    private Runnable r;

    public GeneralEvent(Task parentTask, Runnable r) {
        super(parentTask);
        this.r = r;
    }

    @Override
    public void react() {
        r.run();
    }

}
