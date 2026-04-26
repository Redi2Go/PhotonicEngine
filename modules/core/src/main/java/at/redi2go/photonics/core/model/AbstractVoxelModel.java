package at.redi2go.photonics.core.model;

import at.redi2go.photonics.core.util.VectorUtil;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.Objects;

public abstract class AbstractVoxelModel implements VoxelModel {
    protected final int width, height, depth;

    public AbstractVoxelModel(Vector3ic size) {
        Objects.requireNonNull(size, "size was null");

        if (!VectorUtil.isPowerOf2(size))
            throw new IllegalArgumentException(size + " is not a power of 2!");

        width = size.x();
        height = size.y();
        depth = size.z();
    }

    protected abstract int get(int index);

    protected abstract void set(int index, int value);

    @Override
    public Vector3ic size() {
        return new Vector3i(width, height, depth);
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return VoxelModel.contains(x, y, z, width, height, depth);
    }

    @Override
    public int get(int x, int y, int z) {
        if (!contains(x, y, z))
            throw new IllegalArgumentException("Not in bounds");

        return get(VoxelModel.toVoxelIndex(x, y, z));
    }

    protected void optimize() {
        int length = width * height * depth;

        for (int i = 0; i < length; i++) {
            int entry = get(i);
            if (VoxelEntry.isData(entry)) continue;

            int maxIndex = VoxelModel.toVoxelIndex(width - 1, height - 1, depth - 1);

            set(i,
                    VoxelEntry.toAir(
                            mergeNeighbours(
                                    VoxelModel.shrinkBits(i),
                                    VoxelModel.shrinkBits(i >> 1),
                                    VoxelModel.shrinkBits(i >> 2),
                                    ~maxIndex
                            )
                    )
            );
        }
    }

    protected int mergeNeighbours(int x, int y, int z, int indexMask) {
        int startX = x;
        int startY = y;
        int startZ = z;
        int endX = x + 1;
        int endY = y + 1;
        int endZ = z + 1;

        boolean northMerged = true, southMerged = true, eastMerged = true, westMerged = true, topMerged = true, bottomMerged = true;
        boolean merged;

        do {
            merged = false;

            // North
            if (northMerged && canMergeNorthSouth(startX, startY, endZ, endX, endY, indexMask)) {
                endZ++;

                merged = true;
            } else {
                northMerged = false;
            }

            // South
            if (southMerged && canMergeNorthSouth(startX, startY, startZ - 1, endX, endY, indexMask)) {
                startZ--;

                merged = true;
            } else {
                southMerged = false;
            }

            // East
            if (eastMerged && canMergeEastWest(endX, startY, startZ, endY, endZ, indexMask)) {
                endX++;

                merged = true;
            } else {
                eastMerged = false;
            }

            // West
            if (westMerged && canMergeEastWest(startX - 1, startY, startZ, endY, endZ, indexMask)) {
                startX--;

                merged = true;
            } else {
                westMerged = false;
            }

            // Top
            if (topMerged && canMergeTopBottom(startX, endY, startZ, endX, endZ, indexMask)) {
                endY++;

                merged = true;
            } else {
                topMerged = false;
            }

            // Bottom
            if (bottomMerged && canMergeTopBottom(startX, startY - 1, startZ, endX, endZ, indexMask)) {
                startY--;

                merged = true;
            } else {
                bottomMerged = false;
            }
        } while (merged);

        return VoxelEntry.toAir(startX, startY, startZ, endX, endY, endZ);
    }

    private boolean canMergeEastWest(int x, int startY, int startZ, int endY, int endZ, int indexMask) {
        for (int y = startY; y < endY; y++) {
            for (int z = startZ; z < endZ; z++) {
                var index = VoxelModel.toVoxelIndex(x, y, z);
                if ((index & indexMask) != 0) return false;

                if (VoxelEntry.isData(get(index)))
                    return false;
            }
        }

        return true;
    }

    private boolean canMergeNorthSouth(int startX, int startY, int z, int endX, int endY, int indexMask) {
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                var index = VoxelModel.toVoxelIndex(x, y, z);
                if ((index & indexMask) != 0) return false;

                if (VoxelEntry.isData(get(index)))
                    return false;
            }
        }

        return true;
    }

    private boolean canMergeTopBottom(int startX, int y, int startZ, int endX, int endZ, int indexMask) {
        for (int x = startX; x < endX; x++) {
            for (int z = startZ; z < endZ; z++) {
                var index = VoxelModel.toVoxelIndex(x, y, z);
                if ((index & indexMask) != 0) return false;

                if (VoxelEntry.isData(get(index)))
                    return false;
            }
        }

        return true;
    }
}
