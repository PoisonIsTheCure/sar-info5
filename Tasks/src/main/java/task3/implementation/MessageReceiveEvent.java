package task3.implementation;

import task3.specification.Channel;
import task3.specification.Event;
import task3.specification.MessageQueue;

public class MessageReceiveEvent implements Event {
    private MessageQueue parentMessageQueue;
    private Message message;

    public interface InternalListener {
        void received(byte[] message);
    }

    public MessageReceiveEvent(MessageQueue parentMessageQueue, Message message) {
        this.parentMessageQueue = parentMessageQueue;
        this.message = message;
    }

    @Override
    public void react() {
        parentMessageQueue.getListener().received(message.getMessage());
    }

}
