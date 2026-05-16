package at.redi2go.photonics.core.collect;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

public class EmptyQueue extends AbstractQueue<Object> {
    private static EmptyQueue INSTANCE = new EmptyQueue();

    private EmptyQueue() {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean offer(Object o) {
        return false;
    }

    @Override
    public Object poll() {
        return null;
    }

    @Override
    public Object peek() {
        return null;
    }

    @Override
    public Iterator<Object> iterator() {
        return EmptyIterator.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public static <T> Queue<T> of() {
        return (Queue<T>) INSTANCE;
    }

    private static class EmptyIterator implements Iterator<Object> {
        private static final EmptyIterator INSTANCE = new EmptyIterator();

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new NoSuchElementException();
        }
    }
}
