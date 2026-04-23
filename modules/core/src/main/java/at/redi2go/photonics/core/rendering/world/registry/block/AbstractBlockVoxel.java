package at.redi2go.photonics.core.rendering.world.registry.block;

import at.redi2go.photonics.core.model.AbstractVoxelModel;
import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.RtVoxel;
import at.redi2go.photonics.core.rendering.world.block.BlockVoxel;
import at.redi2go.photonics.core.rendering.world.registry.AbstractHashedObject;
import at.redi2go.photonics.core.rendering.world.registry.BufferBlockRegistry;
import at.redi2go.photonics.core.rendering.world.registry.HashedObject;
import at.redi2go.photonics.core.util.IntPacking;
import org.joml.Vector3ic;

import java.nio.IntBuffer;

public abstract class AbstractBlockVoxel extends AbstractHashedObject implements BlockVoxel {
    private final long hashCode;

    private IntBuffer buffer = null;

    protected AbstractBlockVoxel(
            BufferBlockRegistry registry,
            long hashCode
    ) {
        super(registry);

        this.hashCode = hashCode;
    }

    @Override
    protected long hash() {
        return hashCode;
    }

    public void allocate(int[] data) {
        initMemory(data.length * 4);
        buffer = memory.buffer().asIntBuffer();
        buffer.put(data);

        memory.upload();

        registry.scheduleOptimization(() -> {
            var wrapper = new ModelWrapper();
            wrapper.optimize();

            memory.upload();
        });
    }

    public IntBuffer buffer() {
        return buffer;
    }

    @Override
    public Vector3ic size() {
        return RtVoxel.SIZE_3;
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return VoxelModel.contains(x, y, z, RtVoxel.SIDE_LENGTH, RtVoxel.SIDE_LENGTH, RtVoxel.SIDE_LENGTH);
    }

    @Override
    public int get(int x, int y, int z) {
        return buffer.get(VoxelModel.toVoxelIndex(x, y, z));
    }

    @Override
    protected void dispose() {
        buffer = null;
        // Nothing to release
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AbstractBlockVoxel other && hashCode == other.hashCode;
    }

    private class ModelWrapper extends AbstractVoxelModel {
        public ModelWrapper() {
            super(RtVoxel.SIZE_3);
        }

        @Override
        protected int get(int index) {
            var buffer = AbstractBlockVoxel.this.buffer;
            if (buffer == null) return VoxelModel.makeAirEntry(index);

            return buffer.get(index);
        }

        @Override
        protected void set(int index, int value) {
            var buffer = AbstractBlockVoxel.this.buffer;
            if (buffer == null) return;

            buffer.put(index, value);
        }

        @Override
        public void optimize() {
            super.optimize();
        }
    }
}
