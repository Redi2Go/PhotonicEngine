package at.redi2go.photonics.core.old.world;

import at.redi2go.photonics.core.old.model.VoxelModel;
import org.joml.Vector3i;
import org.joml.Vector3ic;

public interface RtVoxel extends VoxelModel {
    int SIDE_LENGTH = 16;
    Vector3ic SIZE_3 = new Vector3i(SIDE_LENGTH);

    int ENTRIES_SIZE = SIDE_LENGTH * SIDE_LENGTH * SIDE_LENGTH;

    int blockSideLength();

    default Vector3ic blockSize() {
        return new Vector3i(blockSideLength());
    }

    int voxelSideLength();

    default Vector3ic voxelSize() {
        return new Vector3i(voxelSideLength());
    }

    default boolean containsBlock(int x, int y, int z) {
        var size = blockSideLength();

        return VoxelModel.contains(x, y, z, size, size, size);
    }

    default boolean containsBlock(Vector3ic pos) {
        return containsBlock(pos.x(), pos.y(), pos.z());
    }

    default boolean containsVoxel(int x, int y, int z) {
        var size = voxelSideLength();

        return VoxelModel.contains(x, y, z, size, size, size);
    }

    default boolean containsVoxel(Vector3ic pos) {
        return containsVoxel(pos.x(), pos.y(), pos.z());
    }
}
