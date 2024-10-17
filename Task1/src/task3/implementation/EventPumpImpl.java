package task3.implementation;

import task3.specification.Event;
import task3.specification.EventPump;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class EventPumpImpl extends EventPump {

    private final Queue<Event> eventsQueue;
    private Semaphore semaphore = new Semaphore(0);
    private boolean isKilled = false;



    public EventPumpImpl() {
        this.eventsQueue = new LinkedList<>();
        this.setName("EventPump");
    }


    @Override
    public synchronized void post(Event event) {
        if (isKilled) {
            return;
        }
        eventsQueue.add(event);
        semaphore.release();
    }

    @Override
    public void kill() {
        this.isKilled = true;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (isKilled && eventsQueue.isEmpty()) {
                    break;
                }
                semaphore.acquire(); // wait for an event to be posted
                Event event = eventsQueue.poll();
                if (event instanceof task3.specification.GeneralEvent) {
                    ((task3.specification.GeneralEvent) event).react();
                    continue;
                }
                event.react();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
