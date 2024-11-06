package task4.specification;

import org.tinylog.Logger;
import task4.events.GeneralEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class Task implements Runnable {
    private static Task instance = null;
    private boolean isKilled = false;

    // Current running tasks
    public static final List<Task> runningTasks = Collections.synchronizedList(new ArrayList<>());

    private EventPump pump;


    public Task(EventPump pump){
        instance = this;
        this.pump = pump;
        runningTasks.add(this);
    }

    public void post(Event r){
        if (isKilled && r.getParentTask()!=this) {
            throw new IllegalStateException("Task is killed");
        }
        pump.post(r);
    }

    protected void setCurrentTask(Task task){
        instance = task;
    }

    public static Task task(){
        return instance;
    }

    public void kill(){
        if (isKilled){
            return; // already killed
        }
        isKilled = true;
        pump.post(new GeneralEvent(this,() -> {
            runningTasks.remove(this);
            Logger.info("Task " + this + " killed");
        }));
    }

    public boolean killed(){
        return this.isKilled;
    }

}
