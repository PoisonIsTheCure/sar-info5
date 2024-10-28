package task4.specification;

import task4.events.GeneralEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        if (isKilled) {
            throw new IllegalStateException("Task Has been killed!");
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
        this.setCurrentTask(null);
        this.isKilled = true;
        pump.post(new GeneralEvent(() -> runningTasks.remove(this)));
    }

    public boolean killed(){
        return this.isKilled;
    }
}
