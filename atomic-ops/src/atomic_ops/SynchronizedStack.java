package atomic_ops;

public class SynchronizedStack<A> implements Stack<A> {

    private static class Node<A> {

        public final A item;

        public Node<A> next;

        public Node(A item) {
            this.item = item;
        }

    }

    private Node<A> top;

    @Override
    public synchronized void push(A item) {
        System.out.println("Synchronized pushing");
        Node<A> newTop = new Node(item);
        newTop.next = top;
        top = newTop;
    }

    @Override
    public synchronized A pop() {
        System.out.println("Synchronized popping");
        if(top == null) {
            return null;
        } else {
            final A item = top.item;
            top = top.next;
            return item;
        }
    }

}
