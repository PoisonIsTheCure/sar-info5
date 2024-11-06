package task2.implementation;

// Class to store the messages in the queue
class Message {
    private byte[] message;
    private int offset;
    private int length;

    public Message(byte[] message, int offset, int length) {
        this.message = message;
        this.offset = offset;
        this.length = length;
    }

    public byte[] getMessage() {
        return message;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }
}

