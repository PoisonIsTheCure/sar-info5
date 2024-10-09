package task3.implementation;

import task3.specification.Event;
import task3.specification.EventPump;

import java.util.LinkedList;
import java.util.Queue;

public class EventPumpImpl extends EventPump {

    private Queue<Event> messageEventsQueue;

    private Queue<Event> connectionEventsQueue;

    private Queue<Event> acceptEventsQueue;

    private enum EventTypeToHandle {
        MESSAGE,
        CONNECTION,
        ACCEPT
    }

    private EventTypeToHandle eventTypeToHandle;


    private EventPumpImpl() {
        EventPump.eventPumpInstance = this;
        this.eventTypeToHandle = EventTypeToHandle.MESSAGE;
        this.messageEventsQueue = new LinkedList<Event>();
        this.connectionEventsQueue = new LinkedList<Event>();
        this.acceptEventsQueue = new LinkedList<Event>();
    }

    public static void createInstance() {
        if (EventPump.eventPumpInstance == null) {
            EventPump.setInstance(new EventPumpImpl());
        }
    }

    public synchronized void post(Event event) {
        if (event instanceof ConnectEvent) {
            connectionEventsQueue.add(event);
        }
        else if (event instanceof BindEvent) {
            acceptEventsQueue.add(event);
        } else {
            messageEventsQueue.add(event);
        }
    }

    @Override
    public void kill() {
        messageEventsQueue.clear();
        connectionEventsQueue.clear();
        acceptEventsQueue.clear();
    }

    @Override
    public void run() {
        while (true) {
            if (messageEventsQueue.isEmpty() && connectionEventsQueue.isEmpty() && acceptEventsQueue.isEmpty()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            switch (eventTypeToHandle) {
                case MESSAGE:
                    if (!messageEventsQueue.isEmpty()) {
                        messageEventsQueue.poll().react();
                    } else {
                        eventTypeToHandle = EventTypeToHandle.CONNECTION;
                    }
                    break;
                case CONNECTION:
                    if (!connectionEventsQueue.isEmpty()) {
                        // Get the first event in the queue
                        ConnectEvent connectEvent = (ConnectEvent) connectionEventsQueue.poll();
                        // Check if the corresponding broker is available
                        for (Event event : acceptEventsQueue){
                            BindEvent bindEvent = (BindEvent) event;
                            if (bindEvent.getName().equals(connectEvent.getName())){
                                BindEvent correspondingBindEvent = bindEvent;
                                // remove the bind event from the queue
                                acceptEventsQueue.remove(bindEvent);
                                // run the accept in event in a seperate thread (because it's blocking)
                                new Thread(() -> correspondingBindEvent.react()).start();
                                connectEvent.react();
                                break;
                            }
                        }
                    } else {
                        eventTypeToHandle = EventTypeToHandle.ACCEPT;
                    }
                    break;
                case ACCEPT:
                    if (!acceptEventsQueue.isEmpty()) {
                        // Same as above
                        BindEvent bindEvent = (BindEvent) acceptEventsQueue.poll();
                        for (Event event : connectionEventsQueue){
                            ConnectEvent connectEvent = (ConnectEvent) event;
                            if (bindEvent.getName().equals(connectEvent.getName())){
                                ConnectEvent correspondingConnectEvent = connectEvent;
                                connectionEventsQueue.remove(connectEvent);
                                new Thread(() -> correspondingConnectEvent.react()).start();
                                bindEvent.react();
                                break;
                            }
                        }
                    } else {
                        eventTypeToHandle = EventTypeToHandle.MESSAGE;
                    }
                    break;
            }
        }
    }
}
