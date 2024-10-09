package task3.implementation;

import task3.specification.Channel;
import task3.specification.Event;
import task3.specification.MessageQueue;

public class MessageReceiveEvent implements Event {
    private byte[] message;
    private int offset;
    private int length;
    private Channel channel;
    private InternalListener listener;

    public interface InternalListener {
        void received(byte[] message);
    }

    public MessageReceiveEvent(Channel channel, int length , InternalListener listener) {
        this.message = new byte[length];
        this.offset = 0;
        this.length = length;
        this.channel = channel;
        this.listener = listener;
    }

    @Override
    public void react() {
        this.channel.read(message, offset, length);
        listener.received(message);
    }

}
