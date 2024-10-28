package task4.events;

import task4.specification.Event;

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
