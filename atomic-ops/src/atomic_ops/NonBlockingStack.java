package atomic_ops;

import java.util.concurrent.atomic.AtomicReference;

public class NonBlockingStack<A> implements Stack<A> {

    private static class Node<A> {

        public final A item;

        public Node<A> next;

        public Node(A item) {
            this.item = item;
        }

    }

    private AtomicReference<Node<A>> top = new AtomicReference<>();

    @Override
    public void push(A item) {
        System.out.println("Nonblocking pushing");
        Node<A> newHead = new Node(item);
        Node<A> oldHead;
        do {
            oldHead = top.get();
            newHead.next = oldHead;
        } while(!top.compareAndSet(oldHead, newHead));
    }

    @Override
    public A pop() {
        System.out.println("Nonblocking popping");
        Node<A> oldHead;
        Node<A> newHead;
        do {
            oldHead = top.get();
            if(oldHead == null){
                return null;
            }
            newHead = oldHead.next;
        } while(!top.compareAndSet(oldHead, newHead));
        return newHead.item;
    }

}
