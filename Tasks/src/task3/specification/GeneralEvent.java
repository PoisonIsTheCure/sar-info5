package task3.specification;

public class GeneralEvent implements Event {

    private Runnable r;

    public GeneralEvent(Runnable r){
        this.r = r;
    }

    @Override
    public void react() {
        r.run();
    }
}
