package task3.specification;

interface ConnectListener {
    void connected(MessageQueue messageQueue);
    void refused();
}