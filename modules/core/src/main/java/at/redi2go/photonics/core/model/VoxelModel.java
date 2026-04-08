package at.redi2go.photonics.core.model;

import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public interface VoxelModel<S> {
    /**
     * The dimensions of the model. Each component must always be a power of 2.
     */
    Vector3ic size();

    boolean contains(int x, int y, int z);

    default boolean contains(Vector3ic pos) {
        return contains(pos.x(), pos.z(), pos.y());
    }

    int get(int x, int y, int z);

    default int get(Vector3ic pos) {
        return get(pos.x(), pos.y(), pos.z());
    }

    /**
     * Applies {@code transform} to each voxel in this model, and returns the new model.
     *
     * @param transform The transform to apply, where {@link Vector3i} is the x,y,z of the voxel. The value after the transform is used as the new position for the voxel.
     */
    VoxelModel<S> applyTransform(Consumer<Vector3i> transform, IntFunction<S> storageSupplier);

    /**
     * Returns an axis-independent hash, that encodes rotation.
     * Vector components can be hashed together, to create a rotation-independent hash.
     * <br><br>
     * Useful to avoid saving multiple schematics that only differ in rotation.
     *
     * @apiNote Requires {@link VoxelModel#size()} to be 16x16x16
     */
    Optional<Vector3ic> hashVector();

    interface Builder<S> extends VoxelModel<S> {
        void set(int x, int y, int z, int value);

        default void set(Vector3i pos, int value) {
            set(pos.x, pos.y, pos.z, value);
        }

        Builder<S> optimize();

        VoxelModel<S> build();
    }
}
