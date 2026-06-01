package at.redi2go.photonics.core.rendering.world.registry.object;

import at.redi2go.photonics.api.Disposable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class AbstractWorldObject<M extends Disposable> implements WorldObject {
    private static final int CLOSED = -3;
    private static final int UNALLOCATED = -2;
    private static final int ALLOCATING = -1;

    private final ObjectRegistry<WorldObject> registry;
    private volatile int referenceCount = UNALLOCATED;

    private M memory = null;

    @SuppressWarnings("unchecked")
    protected AbstractWorldObject(ObjectRegistry<?> registry) {
        this.registry = (ObjectRegistry<WorldObject>) registry;
    }

    @Override
    public boolean isAllocated() {
        return referenceCount >= 0;
    }

    public void awaitAllocated() {
        int count = referenceCount;
        checkNotClosed(count);

        while (count < 0) {
            Thread.onSpinWait();

            count = referenceCount;
            checkNotClosed(count);
        }
    }

    public void acquireReference() {
        awaitAllocated();

        while (true) {
            int count = referenceCount;
            checkNotClosed(count);

            if (VAR_HANDLE.compareAndSet(this, count, count + 1))
                return;
        }
    }

    public boolean tryAcquireReference() {
        awaitAllocated();

        while (true) {
            int count = referenceCount;
            if (count == CLOSED) return false;

            if (VAR_HANDLE.compareAndSet(this, count, count + 1))
                return true;
        }
    }

    protected void loadDependants(List<WorldObject> output) {

    }

    protected M setMemory(Supplier<M> memorySupplier) {
        while (true) {
            int count = this.referenceCount;
            checkCanAllocate(count);

            if (!VAR_HANDLE.compareAndSet(this, count, ALLOCATING)) continue;

            var memory = memorySupplier.get();
            this.memory = memory;

            List<WorldObject> dependants = new ArrayList<>();
            loadDependants(dependants);

            for (var dependant : dependants) {
                dependant.acquireReference();
                dependant.awaitAllocated();
            }

            if (!VAR_HANDLE.compareAndSet(this, ALLOCATING, 0))
                throw new IllegalStateException("Count was changed during allocation");

            return memory;
        }
    }

    protected @Nullable M memoryOrNull() {
        M memory = this.memory;
        int count = this.referenceCount;

        if (count < 0) return null;
        return memory;
    }

    protected @NonNls M memoryOrThrow() {
        M memory = this.memory;
        int count = referenceCount;

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

        List<WorldObject> dependants = new ArrayList<>();
        loadDependants(dependants);

        for (var dependant : dependants)
            dependant.close();

        return true;
    }

    @Override
    public final void close() {
        while (true) {
            int count = referenceCount;
            checkNotClosed(count);

            if (count == 0)
                throw new IllegalStateException("count was 0");

            if (VAR_HANDLE.compareAndSet(this, count, count - 1)) {
                if (count == 1)
                    registry.enqueueObject(new HandleImpl());

                return;
            }
        }
    }

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

    private class HandleImpl implements Handle<WorldObject> {
        @Override
        public @Nullable WorldObject free() {
            return dispose() ? AbstractWorldObject.this : null;
        }
    }

    private static final VarHandle VAR_HANDLE;

    static {
        try {
            VAR_HANDLE = MethodHandles.lookup()
                    .findVarHandle(AbstractWorldObject.class, "referenceCount", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
