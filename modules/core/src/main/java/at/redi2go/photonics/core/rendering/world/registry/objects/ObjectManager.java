package at.redi2go.photonics.core.rendering.world.registry.objects;

import at.redi2go.photonics.api.Disposable;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ObjectManager {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Queue<Disposable> freeQueue = new ConcurrentLinkedQueue<>();

    private final HeldLock heldReadLock = new HeldLockImpl();

    public HeldLock acquireLock() {
        lock.readLock().lock();
        return heldReadLock;
    }

    public HeldLock acquireLockInterruptibly() throws InterruptedException {
        lock.readLock().lockInterruptibly();
        return heldReadLock;
    }


    public void enqueueObject(Disposable disposable) {
        freeQueue.add(disposable);
    }

    public void freeUnusedObjects() throws InterruptedException {
        lock.writeLock().lockInterruptibly();

        try {
            while (!freeQueue.isEmpty()) {
                var handle = freeQueue.poll();
                if (handle == null) return;

                handle.close();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public interface HeldLock extends AutoCloseable {
        @Override
        void close();
    }

    private class HeldLockImpl implements HeldLock {
        @Override
        public void close() {
            lock.readLock().unlock();
        }
    }
}
