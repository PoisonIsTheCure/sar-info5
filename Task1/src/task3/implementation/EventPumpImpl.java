package task3.implementation;

import task3.specification.Event;
import task3.specification.EventPump;

import java.util.LinkedList;
import java.util.Queue;

public class EventPumpImpl extends EventPump {

    private Queue<Event> eventsQueue;



    public EventPumpImpl() {
        this.eventsQueue = new LinkedList<>();
    }


    @Override
    public void post(Event event) {
        eventsQueue.add(event);
    }

    @Override
    public void kill() {
        eventsQueue.clear();
    }

    @Override
    public void run() {
        while (true) {
            if (eventsQueue.isEmpty()) {
                try {
                    Thread.sleep(1000); // TODO: Replace it by a Semaphore maybe ?
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else {
                Event event = eventsQueue.poll();
                event.react();
            }
        }
    }
}
