package at.redi2go.photonics.core.rendering.world.registry.objects;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.rendering.world.registry.WorldRegistry;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class WorldObject<M extends Disposable> implements Disposable {
    private static final int CLOSED = -3;
    private static final int UNALLOCATED = -2;
    private static final int ALLOCATING = -1;

    protected final WorldRegistry worldRegistry;
    private volatile int referenceCount = UNALLOCATED;

    private M memory = null;

    public WorldObject(WorldRegistry worldRegistry) {
        this.worldRegistry = worldRegistry;
    }

    public boolean isAllocated() {
        return referenceCount >= 0;
    }

    public void awaitAllocated() {
        int count = referenceCount;
        checkNotClosed(count);

        while (count < 0) {
            Thread.onSpinWait();

            count = referenceCount;
            checkCanAllocate(count);
        }
    }

    public void acquireReference() {
        while (true) {
            int count = referenceCount;
            checkNotClosed(count);

            if (VAR_HANDLE.compareAndSet(this, count, count + 1))
                return;
        }
    }

    public boolean tryAcquireReference() {
        while (true) {
            int count = referenceCount;
            if (count == CLOSED) return false;

            if (VAR_HANDLE.compareAndSet(this, count, count + 1))
                return true;
        }
    }

    protected void loadDependants(List<WorldObject<?>> output) {

    }

    protected M setMemory(Supplier<M> memorySupplier) {
        while (true) {
            int count = this.referenceCount;
            checkCanAllocate(count);

            if (!VAR_HANDLE.compareAndSet(this, count, ALLOCATING)) continue;

            var memory = memorySupplier.get();
            this.memory = memory;

            List<WorldObject<?>> dependants = new ArrayList<>();
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

    public @NonNls M memoryOrThrow() {
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

        List<WorldObject<?>> dependants = new ArrayList<>();
        loadDependants(dependants);

        for (var dependant : dependants)
            dependant.close();

        worldRegistry.objectManager().enqueueObject(this);

        return true;
    }

    @Override
    public void close() {
        while (true) {
            int count = referenceCount;
            checkNotClosed(count);

            if (count == 0)
                throw new IllegalStateException("count was 0");

            if (VAR_HANDLE.compareAndSet(this, count, count - 1)) {
                if (count == 1)
                    worldRegistry.objectManager().enqueueObject(new Handle());

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

    private class Handle implements Disposable {
        @Override
        public void close() {
            dispose();
        }
    }

    private static final VarHandle VAR_HANDLE;

    static {
        try {
            VAR_HANDLE = MethodHandles.lookup()
                    .findVarHandle(WorldObject.class, "referenceCount", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
