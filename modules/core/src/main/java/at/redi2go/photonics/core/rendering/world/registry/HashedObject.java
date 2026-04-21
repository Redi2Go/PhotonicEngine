package at.redi2go.photonics.core.rendering.world.registry;

import at.redi2go.photonics.api.Disposable;

public interface HashedObject extends Disposable {
    int count();

    boolean isAllocated();

    default void awaitAllocated() {
        while (!isAllocated())
            Thread.onSpinWait();
    }

    int begin();

    void acquire();

    void free();
}
