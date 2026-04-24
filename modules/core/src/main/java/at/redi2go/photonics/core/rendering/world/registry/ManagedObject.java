package at.redi2go.photonics.core.rendering.world.registry;

import at.redi2go.photonics.api.Disposable;

public interface ManagedObject extends Disposable {
    int count();

    void acquire();

    void free();
}
