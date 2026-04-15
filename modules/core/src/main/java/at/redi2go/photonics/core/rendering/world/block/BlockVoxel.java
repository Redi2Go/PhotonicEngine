package at.redi2go.photonics.core.rendering.world.block;

import at.redi2go.photonics.core.rendering.world.ReferencedObject;
import at.redi2go.photonics.core.rendering.world.RtVoxel;

public interface BlockVoxel extends RtVoxel, ReferencedObject {
    int begin();

    @Override
    default int blockSideLength() {
        return 1;
    }

    @Override
    default int voxelSideLength() {
        return 16;
    }
}
