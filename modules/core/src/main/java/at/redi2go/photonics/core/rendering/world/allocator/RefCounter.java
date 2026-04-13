package at.redi2go.photonics.core.rendering.world.allocator;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.rendering.world.ReferencedObject;

public interface RefCounter extends ReferencedObject, Disposable {
    int count();
}
