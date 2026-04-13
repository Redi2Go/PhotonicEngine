package at.redi2go.photonics.core.rendering.world.allocator;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public abstract class AbstractRefCounter implements RefCounter {
    private volatile int count = 0;
    protected final BufferWorldAllocator allocator;

    protected AbstractRefCounter(BufferWorldAllocator allocator) {
        this.allocator = allocator;
    }

    @Override
    public int count() {
        return count;
    }

    @Override
    public void acquire() {
        while (true) {
            var previous = count;

            if (HANDLE.compareAndSet(this, previous, previous + 1))
                break;
        }
    }

    @Override
    public void release() {
        while (true) {
            var previous = count;
            var newValue = count - 1;
            if (newValue < 0) return;

            if (HANDLE.compareAndSet(this, previous, newValue)) {
                if (newValue == 0)
                    allocator.enqueueFreedObject(this);

            }
        }
    }

    private static final VarHandle HANDLE;

    static {
        try {
            HANDLE = MethodHandles.lookup()
                    .findVarHandle(AbstractRefCounter.class, "count", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
