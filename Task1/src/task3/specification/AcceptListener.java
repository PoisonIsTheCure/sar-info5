package task3.specification;

interface AcceptListener {
    void bind(int port, String name);
    void unbind(int port);
}