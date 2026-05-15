package at.redi2go.photonics.core.rendering.world.registry;

import at.redi2go.photonics.api.Disposable;

public abstract class ManagedObject<T> extends MemoryOwner<T, Disposable> {
    protected ManagedObject(WorldRegistry registry) {
        super(registry);
    }

    protected void acquireDependants() {
        setMemory(NO_MEMORY);
    }
}
