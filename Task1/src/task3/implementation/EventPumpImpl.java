package task3.implementation;

import task3.specification.Event;
import task3.specification.EventPump;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class EventPumpImpl extends EventPump {

    private final Queue<Event> eventsQueue;
    private Semaphore semaphore = new Semaphore(0);



    public EventPumpImpl() {
        this.eventsQueue = new LinkedList<>();
    }


    @Override
    public void post(Event event) {
        eventsQueue.add(event);
        semaphore.release();
    }

    @Override
    public void kill() {
        eventsQueue.clear();
    }

    @Override
    public void run() {
        while (true) {
            try {
                semaphore.acquire(); // wait for an event to be posted
                Event event = eventsQueue.poll();
                event.react();
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
