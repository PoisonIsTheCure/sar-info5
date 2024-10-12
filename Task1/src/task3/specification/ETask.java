package task3.specification;

public abstract class ETask {
    private static ETask instance = null;

    private EventPump pump;

    public ETask(EventPump pump){
        instance = this;
    }

    public void post(Event r){
        //TODO
    }

    protected void setCurrentTask(ETask task){
        instance = task;
    }

    public static ETask task(){
        return instance;
    }

    public void kill(){
        //TODO
    }
    public boolean killed(){
        return false; // TODO
    }
}
