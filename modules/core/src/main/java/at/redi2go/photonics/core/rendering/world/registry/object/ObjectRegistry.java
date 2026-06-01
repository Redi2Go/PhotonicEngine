package at.redi2go.photonics.core.rendering.world.registry.object;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

public class ObjectRegistry<T extends WorldObject> {
    private final ReadWriteLock lock;
    private final HeldLock heldReadLock = new HeldLockImpl();

    private final ConcurrentHashMap<Object, T> cache = new ConcurrentHashMap<>();
    private final Queue<WorldObject.Handle<T>> freeQueue = new ConcurrentLinkedQueue<>();

    public ObjectRegistry(ReadWriteLock lock) {
        this.lock = lock;
    }

    public HeldLock acquireLock() {
        lock.readLock().lock();
        return heldReadLock;
    }

    public HeldLock acquireLockInterruptibly() throws InterruptedException {
        lock.readLock().lockInterruptibly();
        return heldReadLock;
    }

    protected <K> @WeakValue T cacheObject(
            K key,
            Function<K, T> supplier,
            Consumer<T> allocator
    ) {
        var value = cache.get(key);
        if (value != null) return value;

        var newValue = supplier.apply(key);
        var result = cache.putIfAbsent(newValue, newValue);
        if (result == null) {
            allocator.accept(newValue);
            return newValue;
        }

        return result;
    }

    protected @WeakValue T cacheObject(T value, Consumer<T> allocator) {
        return cacheObject(
                value,
                (e) -> e,
                allocator
        );
    }

    protected void removeObject(T value) {
        cache.remove(value);
    }

    public boolean hasEnqueuedObject() {
        return !freeQueue.isEmpty();
    }

    void enqueueObject(WorldObject.Handle<T> value) {
        freeQueue.add(value);
    }

    public void freeUnusedObjects() throws InterruptedException {
        lock.writeLock().lockInterruptibly();

        try {
            while (!freeQueue.isEmpty()) {
                var handle = freeQueue.poll();
                if (handle == null) return;

                var value = handle.free();
                if (value != null)
                    removeObject(value);
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
