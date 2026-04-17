package at.redi2go.photonics.core.rendering.world.allocator;

import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public abstract class AbstractHashedObject implements HashedObject {
    private volatile int count = 0;
    protected final BufferWorldAllocator allocator;

    protected MemoryView memory;

    protected AbstractHashedObject(BufferWorldAllocator allocator) {
        this.allocator = allocator;
    }

    protected abstract long hash();

    @Override
    public int count() {
        return count;
    }

    @Override
    public boolean isAllocated() {
        return memory != null;
    }

    @Override
    public int begin() {
        return MemoryView.intBufferBegin(memory);
    }

    protected void initMemory(int byteSize) {
        memory = allocator.allocate(byteSize);
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
    public void close() {
        while (true) {
            var previous = count;
            var newValue = count - 1;
            if (newValue < 0) return;

            if (HANDLE.compareAndSet(this, previous, newValue)) {
                if (newValue == 0)
                    allocator.freeHashedObject(this);

            }
        }
    }

    protected abstract void dispose();

    @Override
    public void free() {
        if (memory == null) return;

        memory.close();
        dispose();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(hash());
    }

    @Override
    public boolean equals(Object obj) {
        return getClass() == obj.getClass() && hash() == ((AbstractHashedObject) obj).hash();
    }

    private static final VarHandle HANDLE;

    static {
        try {
            HANDLE = MethodHandles.lookup()
                    .findVarHandle(AbstractHashedObject.class, "count", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
