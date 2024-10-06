package task3.specification;

import java.util.LinkedList;
import java.util.List;

public abstract class Executor extends Thread{
    List<Runnable> queue;

    public Executor(){
        queue = new LinkedList<Runnable>();
    }

    @Override
    public synchronized void run(){
        Runnable r;
        while(true) {
            r = queue.remove(0);
            while (r!=null) {
                r.run();
                r = queue.remove(0);
            }
            sleep();
        }
    }

    public synchronized void post(Runnable r){
        queue.add(r);
        notify();
    }

    public void sleep(){
        try {
            wait();
        } catch (InterruptedException e) {
            // Nothing to do here
        }
    }
}
