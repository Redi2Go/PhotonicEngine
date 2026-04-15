package at.redi2go.photonics.core.rendering.world.allocator;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.rendering.world.ReferencedObject;

public interface HashedObject extends ReferencedObject, Disposable {
    int count();

    boolean isAllocated();

    default void awaitAllocated() {
        while (!isAllocated())
            Thread.onSpinWait();
    }

    int begin();
}
