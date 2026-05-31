package at.redi2go.photonics.core.rendering.world.registry.object;

import at.redi2go.photonics.api.Disposable;
import org.jetbrains.annotations.Nullable;

public interface WorldObject extends Disposable {
    boolean isAllocated();

    void awaitAllocated();

    void acquireReference();

    boolean tryAcquireReference();

    interface Handle<T extends WorldObject> {
        @Nullable T free();
    }
}
