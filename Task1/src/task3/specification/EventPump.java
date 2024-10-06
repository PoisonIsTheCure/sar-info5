package task3.specification;

public abstract class EventPump {

    public abstract void post(Event event);

    public abstract void start();

    public abstract void kill();
}
