package at.redi2go.photonics.core.rendering.world.registry;

import at.redi2go.photonics.api.Disposable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class MemoryOwner<T, M extends Disposable> {
    public static final Disposable NO_MEMORY = () -> { };

    private static final int CLOSED = -3;
    private static final int UNALLOCATED = -2;
    private static final int ALLOCATING = -1;

    private static final VarHandle VAR_HANDLE;

    protected final WorldRegistry registry;

    private M memory = null;
    private volatile int count = UNALLOCATED;

    private final ManagedRefImpl managedRef = new ManagedRefImpl();
    private final RefImpl ref = new RefImpl();

    protected MemoryOwner(WorldRegistry registry) {
        this.registry = registry;
    }

    protected abstract void loadDependants(List<ManagedRef<?>> output);

    protected abstract T getWrappedValue();

    protected void setMemory(M memory) {
        while (true) {
            int count = this.count;
            checkCanAllocate(count);

            if (!VAR_HANDLE.compareAndSet(this, count, ALLOCATING)) continue;

            this.memory = memory;

            List<ManagedRef<?>> dependants = new ArrayList<>();
            loadDependants(dependants);

            for (var dependant : dependants) {
                ((MemoryOwner<?, ?>.ManagedRefImpl) dependant).awaitAllocated();
                dependant.acquire();
            }

            if (!VAR_HANDLE.compareAndSet(this, ALLOCATING, 0))
                throw new IllegalStateException("Count was changed during allocation");

            return;
        }
    }

    protected @Nullable M memoryOrNull() {
        M memory = this.memory;
        int count = this.count;

        if (count < 0) return null;
        return memory;
    }

    public @NonNls M memoryOrThrow() {
        M memory = this.memory;
        int count = this.count;

        return switch (count) {
            case CLOSED -> throw new IllegalStateException("closed");
            case UNALLOCATED -> throw new IllegalStateException("not allocated");
            case ALLOCATING -> throw new IllegalStateException("allocating");

            default -> Objects.requireNonNull(memory, "memory was null");
        };
    }

    protected boolean dispose() {
        if (!VAR_HANDLE.compareAndSet(this, 0, CLOSED)) return false;

        memory.close();
        memory = null;

        List<ManagedRef<?>> dependants = new ArrayList<>();
        loadDependants(dependants);

        for (var dependant : dependants)
            ((MemoryOwner<?, ?>.ManagedRefImpl) dependant).close();

        registry.removeObject(this);

        return true;
    }

    // Ref

    public final Ref<T> makeRef() {
        incrementCount();
        return ref;
    }

    public final ManagedRef<T> makeManagedRef() {
        return managedRef;
    }


    // Count

    private void incrementCount() {
        while (true) {
            int count = this.count;
            checkNotClosed(count);

            if (VAR_HANDLE.compareAndSet(this, count, count + 1))
                return;
        }
    }

    private void decrementCount() {
        while (true) {
            int count = this.count;
            checkNotClosed(count);

            if (count == 0)
                throw new IllegalStateException("count was 0");

            if (VAR_HANDLE.compareAndSet(this, count, count - 1)) {
                if (count == 1)
                    registry.freeQueue.add(new Handle());

                return;
            }
        }
    }

    public boolean isAllocated() {
        return this.count >= 0;
    }

    public void awaitAllocated() {
        int count = this.count;
        checkNotClosed(count);

        while (count < 0) {
            Thread.onSpinWait();

            count = this.count;
            checkCanAllocate(count);
        }
    }


    // Checks

    private void checkNotClosed(int count) {
        if (count == CLOSED)
            throw new IllegalStateException("closed");
    }

    private void checkCanAllocate(int count) {
        if (count == CLOSED)
            throw new IllegalStateException("closed");
        else if (count != UNALLOCATED)
            throw new IllegalStateException("already allocated");
    }

    @Override
    public int hashCode() {
        var wrapped = getWrappedValue();
        if (wrapped == this)
            return System.identityHashCode(this);

        return wrapped.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MemoryOwner<?, ?> other && getWrappedValue().equals(other.getWrappedValue());
    }

    private class ManagedRefImpl implements ManagedRef<T> {
        public void acquire() {
            incrementCount();
        }

        @Override
        public Ref<T> elevate() {
            incrementCount();
            return ref;
        }

        @Override
        public T get() {
            return MemoryOwner.this.getWrappedValue();
        }

        protected void awaitAllocated() {
            MemoryOwner.this.awaitAllocated();
        }

        public void close() {
            MemoryOwner.this.decrementCount();
        }

        @Override
        public void decrementCount() {
            MemoryOwner.this.decrementCount();
        }
    }

    private class RefImpl implements Ref<T> {
        @Override
        public T get() {
            return MemoryOwner.this.getWrappedValue();
        }

        protected void awaitAllocated() {
            MemoryOwner.this.awaitAllocated();
        }

        @Override
        public void close() {
            decrementCount();
        }
    }

    private class Handle implements Disposable {
        @Override
        public void close() {
            dispose();
        }
    }

    public interface Ref<T> extends Disposable {
        @NonNls
        T get();
    }

    public interface ManagedRef<T> {
        void acquire();

        Ref<T> elevate();

        @NonNls
        T get();

        void decrementCount();
    }

    static {
        try {
            VAR_HANDLE = MethodHandles.lookup()
                    .findVarHandle(MemoryOwner.class, "count", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
