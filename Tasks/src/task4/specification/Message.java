package task4.specification;

import java.nio.ByteBuffer;

// Class to store the messages in the queue
public class Message {
    public byte[] message;
    public int offset;
    public int length;

    public byte[] lengthBytes = new byte[4];
    public int lengthOffset = 0;

    public MessageSendState sendState = MessageSendState.SENDING_LENGTH;
    public MessageReceiveState receiveState = MessageReceiveState.RECEIVING_LENGTH;

    public enum MessageSendState {
        SENDING_LENGTH,
        SENDING_MESSAGE,
        FINISHED
    }

    public enum MessageReceiveState {
        RECEIVING_LENGTH,
        RECEIVING_MESSAGE,
        FINISHED
    }


    public Message(byte[] message, int offset, int length) {
        this.message = message;
        this.offset = offset;
        this.length = length;
        this.lengthBytes = intToByteArray(length);
    }

    private byte[] intToByteArray(int length) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(length);
        return buffer.array();
    }

    public int byteArrayToInt(byte[] byteArray) {
        if (byteArray == null || byteArray.length != 4) {
            throw new IllegalArgumentException("Invalid byte array size. Expected 4 bytes.");
        }
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        return buffer.getInt();
    }

}

