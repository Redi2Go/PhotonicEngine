package at.redi2go.photonics.core.rendering.world.registry.block;

import at.redi2go.photonics.core.rendering.world.allocator.WorldAllocator;
import at.redi2go.photonics.core.rendering.world.registry.object.WorldObject;

public interface BlockObject extends WorldObject {
    void allocate(WorldAllocator allocator);
}
