package at.redi2go.photonics.core.rendering.world.block;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.core.model.AbstractVoxelModel;
import at.redi2go.photonics.core.rendering.world.ReferencedObject;
import at.redi2go.photonics.core.rendering.world.RtVoxel;

import java.nio.IntBuffer;

public interface BlockVoxel extends RtVoxel, ReferencedObject {
    @Override
    default int blockSideLength() {
        return 1;
    }

    @Override
    default int voxelSideLength() {
        return 16;
    }
}
