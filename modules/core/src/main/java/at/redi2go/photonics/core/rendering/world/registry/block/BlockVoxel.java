package at.redi2go.photonics.core.rendering.world.registry.block;

import at.redi2go.photonics.core.model.AbstractVoxelModel;
import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.RtVoxel;
import at.redi2go.photonics.core.rendering.world.allocator.BlockVoxelMemory;
import at.redi2go.photonics.core.rendering.world.registry.WorldRegistry;
import at.redi2go.photonics.core.rendering.world.registry.objects.WorldObject;
import org.joml.Vector3ic;

public class BlockVoxel extends WorldObject<BlockVoxelMemory> implements RtVoxel {
    private final long hashCode;

    public BlockVoxel(WorldRegistry worldRegistry, long hashCode) {
        super(worldRegistry);

        this.hashCode = hashCode;
    }

    public int entryData() {
        return memoryOrThrow().entryData();
    }

    public void allocate(int[] data) {
        var memory = setMemory(() -> worldRegistry.worldAllocator().allocateBlockVoxel());

        memory.setData(data);
        memory.upload();

        var optimizationService = worldRegistry.optimizationService();
        optimizationService.scheduleOptimization(() -> {
            var wrapper = new ModelWrapper(data);
            wrapper.optimize();

            if (!tryAcquireReference()) return;

            try {
                memory.setData(data);
                optimizationService.scheduleUpload(memory::upload);
            } finally {
                close();
            }
        });
    }

    @Override
    public int blockSideLength() {
        return 1;
    }

    @Override
    public int voxelSideLength() {
        return 16;
    }

    @Override
    public Vector3ic size() {
        return RtVoxel.SIZE_3;
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return VoxelModel.contains(x, y, z, 16, 16, 16);
    }

    @Override
    public int get(int x, int y, int z) {
        return memoryOrThrow().getEntry(
                VoxelModel.toVoxelIndex(
                        x & 15,
                        y & 15,
                        z & 15
                )
        );
    }

    @Override
    public int hashCode() {
        return Long.hashCode(hashCode);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BlockVoxel other && other.hashCode == hashCode;
    }

    private static class ModelWrapper extends AbstractVoxelModel {
        int[] data;

        public ModelWrapper(int[] data) {
            super(RtVoxel.SIZE_3);

            this.data = data;
        }

        @Override
        protected int get(int index) {
            return data[index];
        }

        @Override
        protected void set(int index, int value) {
            data[index] = value;
        }

        @Override
        public void optimize() {
            super.optimize();
        }
    }
}
