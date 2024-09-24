package task1.implementation;

import task1.CircularBuffer;

import java.util.concurrent.locks.ReentrantLock;

public class BufferController {
    private final CircularBuffer buffer;

    public BufferController(int capacity) {
        this.buffer = new CircularBuffer(capacity);
    }

    public synchronized void push(byte elem) throws InterruptedException {
        while (buffer.full()) {
            wait(); // Waits on the monitor of this BufferController instance
        }
        buffer.push(elem);
        notifyAll(); // Notifies all threads waiting on this instance's monitor
    }

    public synchronized byte pull() throws InterruptedException {
        while (buffer.empty()) {
            wait(); // Waits on the monitor of this BufferController instance
        }
        byte elem = buffer.pull();
        notifyAll(); // Notifies all threads waiting on this instance's monitor
        return elem;
    }

    public boolean full() {
        return buffer.full();
    }

    public boolean empty() {
        return buffer.empty();
    }
}
