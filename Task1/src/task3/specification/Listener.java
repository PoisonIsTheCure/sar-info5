package task3.specification;

public interface Listener {

    void received(byte[] msg);

    void closed();
}
