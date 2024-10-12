package task3.implementation;

import task3.specification.*;

import java.io.IOException;

public class QueueBrokerImpl extends QueueBroker {

    private EventPump eventPump;
    private Task brokerTask;
    private ETask parentTask;

    public QueueBrokerImpl(String name) {
        this.brokerTask = new TaskImpl(this, new Runnable() {
            @Override
            public void run() {
                QueueBrokerImpl.super.broker = new BrokerImpl(name);
            }
        });
        this.parentTask = ETask.task();
    }


    @Override
    public String name() {
        return this.broker.getName();
    }



    @Override
    public boolean connect(String name, int port, ConnectListener listener) {
        ConnectEvent event = new ConnectEvent(name, port, listener);
        eventPump.post(event);
        return true;
    }

    @Override
    public void bind(int port, AcceptListener listener) {
        BindEvent event = new BindEvent(this.broker,port, listener);
        eventPump.post(event);
    }

    @Override
    public void unbind(int port) {
        UnBindEvent event = new UnBindEvent(this.broker,port);
        eventPump.post(event);
    }


}