package task4.implementation;

import task4.events.BindEvent;
import task4.specification.*;

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
        Task.task().post(event);
        return true;
    }

    @Override
    public void bind(int port, AcceptListener listener) {
        BindEvent event = new BindEvent(this.broker,port, listener);
        Task.task().post(event);
    }

    @Override
    public void unbind(int port) {
        BrokerImpl.UnBindEvent event = new BrokerImpl.UnBindEvent(this.broker,port);
        Task.task().post(event);
    }


}