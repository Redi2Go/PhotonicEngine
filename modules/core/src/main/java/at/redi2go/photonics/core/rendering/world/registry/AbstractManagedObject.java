package at.redi2go.photonics.core.rendering.world.registry;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public abstract class AbstractManagedObject implements ManagedObject {
    private volatile int count = 0;
    protected final BufferBlockRegistry registry;

    protected boolean isOpen = false;

    protected AbstractManagedObject(BufferBlockRegistry registry) {
        this.registry = registry;
    }

    protected abstract long hash();

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
    public void close() {
        while (true) {
            var previous = count;
            var newValue = count - 1;
            if (newValue < 0) return;

            if (HANDLE.compareAndSet(this, previous, newValue)) {
                if (newValue == 0)
                    registry.freeObject(this);
            }
        }
    }

    protected abstract void dispose();

    @Override
    public void free() {
        if (!isOpen) return;

        isOpen = false;
        dispose();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(hash());
    }

    @Override
    public boolean equals(Object obj) {
        return getClass() == obj.getClass() && hash() == ((AbstractManagedObject) obj).hash();
    }

    private static final VarHandle HANDLE;

    static {
        try {
            HANDLE = MethodHandles.lookup()
                    .findVarHandle(AbstractManagedObject.class, "count", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
