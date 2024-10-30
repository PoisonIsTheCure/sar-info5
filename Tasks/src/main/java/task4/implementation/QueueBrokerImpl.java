package task4.implementation;

import org.tinylog.Logger;
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
        try {
            this.broker.connect(name, port, listener);
        } catch (Exception e) {
            Logger.error("Failed to connect to broker: " + name + " on port " + port, e);
            return false;
        }
        return true;
    }

    @Override
    public void bind(int port, AcceptListener listener) {
        try {
            this.broker.bind(port, listener);
        } catch (Exception e) {
            Logger.error("Failed to bind to port: " + port, e);
        }
    }

    @Override
    public void unbind(int port) {
        this.broker.unbind(port);
    }


}