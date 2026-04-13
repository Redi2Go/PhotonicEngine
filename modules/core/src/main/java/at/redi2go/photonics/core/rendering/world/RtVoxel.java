package at.redi2go.photonics.core.rendering.world;

import at.redi2go.photonics.core.model.VoxelModel;
import org.joml.Vector3i;
import org.joml.Vector3ic;

public interface RtVoxel extends VoxelModel {
    int SIZE = 16;
    Vector3ic SIZE_3 = new Vector3i(SIZE);

    int ENTRIES_SIZE = SIZE * SIZE * SIZE;

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

        return ((x | y | z |
                ~(x - size) |
                ~(y - size) |
                ~(z - size)
        ) & Integer.MIN_VALUE) == 0;
    }

    default boolean containsBlock(Vector3ic pos) {
        return containsBlock(pos.x(), pos.y(), pos.z());
    }

    default boolean containsVoxel(int x, int y, int z) {
        var size = voxelSideLength();

        return ((x | y | z |
                ~(x - size) |
                ~(y - size) |
                ~(z - size)
        ) & Integer.MIN_VALUE) == 0;
    }

    default boolean containsVoxel(Vector3ic pos) {
        return containsVoxel(pos.x(), pos.y(), pos.z());
    }
}
