package at.redi2go.photonics.core.model;

import at.redi2go.photonics.core.util.VectorUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public abstract class AbstractVoxelModel<S> implements VoxelModel<S> {
    protected final int width, height, depth;

    public AbstractVoxelModel(Vector3ic size) {
        Objects.requireNonNull(size, "size was null");

        if (!VectorUtil.isPowerOf2(size))
            throw new IllegalArgumentException(size + " is not a power of 2!");

        width = size.x();
        height = size.y();
        depth = size.z();
    }

    protected abstract S getStorage();

    protected abstract VoxelModel<S> createModel(Vector3ic size, S storage, @Nullable Vector3ic hash);

    protected abstract int get(S storage, int index);

    protected abstract void set(S storage, int index, int value);

    @Override
    public Vector3ic size() {
        return new Vector3i(width, height, depth);
    }

    @Override
    public boolean contains(int x, int y, int z) {
        // Dumb way to do this check without using branches
        // From my testing its around 20x faster than checking <c> >= 0 && <c> < size.<c>() x3
        return ((x | y | z |
                        ~(x - width) |
                        ~(y - height) |
                        ~(z - depth)
        ) & Integer.MIN_VALUE) == 0;
    }

    @Override
    public int get(int x, int y, int z) {
        if (!contains(x, y, z))
            return Integer.MAX_VALUE;

        return get(getStorage(), toVoxelIndex(x, y, z));
    }

    @Override
    public VoxelModel<S> applyTransform(Consumer<Vector3i> transform, IntFunction<S> storageSupplier) {
        final var newStorage = storageSupplier.apply(width * height * depth);
        final var oldStorage = getStorage();

        final var voxel = new Vector3i();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z= 0; z < depth; z++) {
                    voxel.x = x;
                    voxel.y = y;
                    voxel.z = z;

                    transform.accept(voxel);

                    var index = toVoxelIndex(x, y, z);
                    set(newStorage, index, get(oldStorage, index));
                }
            }
        }

        return createModel(size(), newStorage, hashVector().orElse(null));
    }

    private static int expandBits(int value) {
        int v = value & 0x1F;
        v = (v | (v << 16)) & 0x030000FF;
        v = (v | (v << 8)) & 0x0300F00F;
        v = (v | (v << 4)) & 0x030C30C3;
        v = (v | (v << 2)) & 0x09249249;
        return v;
    }

    private static int toVoxelIndex(int x, int y, int z) {
        return expandBits(x) | (expandBits(y) << 1) | (expandBits(z) << 2);
    }


    protected void performSetup() {
        var storage = getStorage();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    int index = toVoxelIndex(x, y, z);
                    int entry = get(storage, index);

                    if (entry == 0) {
                        entry = x | (y << 5) | (z << 10);
                        entry |= entry << 15;
                    } else {
                        entry = -entry;
                    }

                    set(storage, index, entry);
                }
            }
        }
    }
}
