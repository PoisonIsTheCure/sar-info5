package task4.implementation;

import task4.specification.*;

import java.util.LinkedList;
import java.util.Queue;

public class MessageQueueImpl extends MessageQueue {

    private final Channel channel;
    private final boolean isClosed;
    private final Task parentTask;
    private final Queue<Message> messages = new LinkedList<Message>();
    private SendState sendState = SendState.IDLE;
    private ReceiveState receiveState = ReceiveState.IDLE;
    private int pendingReadNotification = 0;
    private Rdv rdv;


    enum SendState {
        SENDING_MESSAGE,
        IDLE
    }

    enum ReceiveState {
        RECEIVING_MESSAGE,
        IDLE
    }


    public MessageQueueImpl(Rdv rdv, Channel channel) {
        this.channel = channel;
        this.rdv = rdv;
        this.isClosed = false;
        this.parentTask = Task.task();
    }

    @Override
    public void setListener(MessageQueue.Listener l) {
        this.listener = l;
    }

    @Override
    public void send(Message msg) throws DisconnectedException {
        if (isClosed) {
            throw new DisconnectedException("MessageQueue is closed");
        }

        // add the message to the queue
        messages.add(msg);

        // if the queue is idle, send the message
        if (sendState == SendState.IDLE) {
            Message message = messages.poll();
            sendState = SendState.SENDING_MESSAGE;
            parentTask.post(new SendEvent(parentTask, message));
        }
    }

    @Override
    public void close() {
        // TODO: Implement this method
    }


    @Override
    public boolean closed() {
        return isClosed;
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    protected void notifyRead() {
        if (this.receiveState == ReceiveState.IDLE) {
            this.receiveState = ReceiveState.RECEIVING_MESSAGE;
            this.parentTask.post(new ReadEvent(parentTask));
        } else {
            this.pendingReadNotification++;
        }
    }

    /**
     * The classes are Event Handler classes that are used to handle the sending and receiving of messages
     */

    private class SendEvent extends Event {
        private final Message msg;

        public SendEvent(Task parentTask, Message msg) {
            super(parentTask);
            this.msg = msg;
        }

        @Override
        public void react() {
            int written = 0;
            switch (msg.sendState){
                case SENDING_LENGTH:
                    // Notify the receiver that data is available
                    MessageQueueImpl.this.rdv.notifyMessageQueueRead(MessageQueueImpl.this);

                    // Write the message length (4 bytes)
                    written =  channel.write(msg.lengthBytes, msg.lengthOffset, 4 - msg.lengthOffset);
                    msg.lengthOffset += written;
                    if (msg.lengthOffset < 4) {
                        parentTask.post(this);
                        break;
                    } else {
                        msg.sendState = Message.MessageSendState.SENDING_MESSAGE;
                    }

                    // fall through, break is not needed
                case SENDING_MESSAGE:
                    written = channel.write(msg.message, msg.offset, msg.length - msg.offset);
                    msg.offset += written;
                    if (msg.offset < msg.length) {
                        parentTask.post(this);
                        break;
                    } else {
                        msg.sendState = Message.MessageSendState.FINISHED;
                    }
                    
                    // fall through, break is not needed
                case FINISHED:
                    // Message fully sent, notify the listener
                    MessageQueueImpl.this.listener.sent(msg);

                    // Check if there are pending messages
                    if (!messages.isEmpty()) {
                        Message next = messages.poll();
                        sendState = SendState.SENDING_MESSAGE;
                        parentTask.post(new SendEvent(parentTask, next));
                    } else {
                        sendState = SendState.IDLE;
                    }
                    break;
            }
        }
    }

    private class ReadEvent extends Event {
        private final Message msg;

        public ReadEvent(Task parentTask) {
            super(parentTask);
            this.msg = new Message(new byte[4], 0, 4);
        }

        @Override
        public void react() {
            int bytesRead = 0;
            switch (msg.receiveState) {
                case RECEIVING_LENGTH:
                    // Read the message length (4 bytes)
                    bytesRead = channel.read(msg.lengthBytes, msg.lengthOffset, 4 - msg.lengthOffset);
                    msg.lengthOffset += bytesRead;

                    if (msg.lengthOffset < 4) {
                        // Repost to complete reading length
                        parentTask.post(this);
                        return;
                    } else {
                        // Length fully read, proceed to read the message
                        msg.length = msg.byteArrayToInt(msg.lengthBytes);
                        msg.message = new byte[msg.length]; // Allocate space for the message
                        msg.receiveState = Message.MessageReceiveState.RECEIVING_MESSAGE;
                    }
                    // Fall-through to receive the message content
                case RECEIVING_MESSAGE:
                    // Read the actual message content
                    bytesRead = channel.read(msg.message, msg.offset, msg.length - msg.offset);
                    msg.offset += bytesRead;

                    if (msg.offset < msg.length) {
                        // If message is not fully read, repost to continue
                        parentTask.post(this);
                        break;

                    } else {
                        // Fully received, mark as finished
                        msg.receiveState = Message.MessageReceiveState.FINISHED;
                        MessageQueueImpl.this.listener.received(msg.message);
                    }

                    // Fall-through to finish the message
                case FINISHED:
                    if (pendingReadNotification > 0) {
                        // If there are pending read notifications, repost to handle them
                        parentTask.post(new ReadEvent(parentTask));
                        pendingReadNotification--;
                    } else {
                        // Message fully read and processed, set idle state
                        receiveState = ReceiveState.IDLE;
                    }
                    break;
            }
        }
    }

    public static class MQCloseEvent extends Event {

        MQCloseEvent(Task parentTask) {
            super(parentTask);
        }

        @Override
        public void react() {

        }
    }
}