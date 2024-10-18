package task3.implementation;

import task3.specification.*;

import java.io.IOException;

public class QueueBrokerImpl extends QueueBroker {

    public QueueBrokerImpl(String name) {
       super.broker = new BrokerImpl(name);
    }


    @Override
    public String name() {
        return this.broker.getName();
    }



    @Override
    public boolean connect(String name, int port, ConnectListener listener) {
        ConnectEvent event = new ConnectEvent(name, port, listener, this.broker);
        ETask.task().post(event);
        return true;
    }

    @Override
    public void bind(int port, AcceptListener listener) {
        BindEvent event = new BindEvent(this.broker,port, listener);
        ETask.task().post(event);
    }

    @Override
    public void unbind(int port) {
        UnBindEvent event = new UnBindEvent(this.broker,port);
        ETask.task().post(event);
    }


}