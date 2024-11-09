package task3.specification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ETask implements Runnable{
    private static ETask instance = null;
    private boolean iskilled = false;

    // Current running tasks
    public static final List<ETask> runningTasks = Collections.synchronizedList(new ArrayList<>());

    private EventPump pump;


    public ETask(EventPump pump){
        instance = this;
        this.pump = pump;
        runningTasks.add(this);
    }

    public void post(Event r){
        pump.post(r);
    }

    protected void setCurrentTask(ETask task){
        instance = task;
    }

    public static ETask task(){
        return instance;
    }

    public void kill(){
        this.setCurrentTask(null);
        this.iskilled = true;
        pump.post(new GeneralEvent(() -> runningTasks.remove(this)));
    }

    public boolean killed(){
        return this.iskilled;
    }
}
