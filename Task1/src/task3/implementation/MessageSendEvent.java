package task3.implementation;

import task3.specification.Channel;
import task3.specification.Event;

import java.nio.ByteBuffer;

public class MessageSendEvent implements Event {
    private byte[] message;
    private int offset;
    private int length;
    private Channel channel;

    public MessageSendEvent(byte[] message, int offset, int length, Channel channel) {
        this.message = message;
        this.offset = offset;
        this.length = length;
        this.channel = channel;
    }

    @Override
    public void react() {
        // Send the Length of the message as a byte array of 4 bytes
        byte[] lengthBytes = intToByteArray(length);
        channel.write(lengthBytes, 0, lengthBytes.length);
        // Normally if everything is correctly set, the write doesn't block the thread
        channel.write(message, offset, length);
    }

    private byte[] intToByteArray(int length) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(length);
        return buffer.array();
    }
}
