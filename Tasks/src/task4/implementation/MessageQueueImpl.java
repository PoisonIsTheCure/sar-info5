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
    private final InternalSendListener internalSendListener;
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

    private interface InternalSendListener {
        void sent(Message msg);
    }


    public MessageQueueImpl(Rdv rdv, Channel channel) {
        this.channel = channel;
        this.rdv = rdv;

        // set the channel listener
        this.channel.setChannelReadListener(
                new Channel.ChannelReadListener() {
                    @Override
                    public void readDataAvailable() {
                        if (receiveState == ReceiveState.IDLE) {
                            receiveState = ReceiveState.RECEIVING_MESSAGE;
                            pendingReadNotification--;
                            parentTask.post(new ReadEvent());
                        } else {
                            // Increment pending notifications if already receiving
                            pendingReadNotification++;
                        }
                    }
                }
        );
        this.isClosed = false;
        this.parentTask = Task.task();

        // The following Listener aims to notify the MessageQueueImpl that the message has been sent
        this.internalSendListener = new InternalSendListener() {
            @Override
            public void sent(Message msg) {
                // Notify the listener that the message has been sent
                if (listener != null) {
                    listener.sent(msg);
                }

                sendState = SendState.IDLE;
                if (!messages.isEmpty()) {
                    Message next = messages.poll();
                    sendState = SendState.SENDING_MESSAGE;
                    parentTask.post(new SendEvent(next, internalSendListener));
                }
            }
        };
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
            parentTask.post(new SendEvent(message, internalSendListener));
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

    private class SendEvent implements Event {
        private final Message msg;
        private final InternalSendListener internalSendListener;

        public SendEvent(Message msg, InternalSendListener listener) {
            this.msg = msg;
            this.internalSendListener = listener;
        }

        @Override
        public void react() {
            int written = 0;
            switch (msg.sendState){
                case SENDING_LENGTH:
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
                    internalSendListener.sent(msg);
                    break;
            }
        }
    }

    private class ReadEvent implements Event {
        private final Message msg;

        public ReadEvent() {
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
                    // Message fully read and processed, set idle state
                    receiveState = ReceiveState.IDLE;
                    break;
            }
        }
    }

    public static class MQCloseEvent implements Event {

        @Override
        public void react() {

        }
    }
}