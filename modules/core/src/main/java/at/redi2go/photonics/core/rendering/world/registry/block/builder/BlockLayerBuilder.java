package at.redi2go.photonics.core.rendering.world.registry.block.builder;

import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryMemory;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockLayer;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockRegistry;
import at.redi2go.photonics.core.rendering.world.registry.block.VoxelLayer;
import at.redi2go.photonics.core.rendering.world.registry.object.WeakValue;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeEntry;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeNode;
import org.joml.Vector3i;

public class BlockLayerBuilder extends VoxelTreeNode {
    private final Vector3i minVoxel = new Vector3i(Integer.MAX_VALUE);
    private final Vector3i maxVoxel = new Vector3i(Integer.MIN_VALUE);

    protected BlockLayerBuilder() {
        super(BLOCK_DEPTH);
    }

    @Override
    protected VoxelTreeNode createNode(int x, int y, int z) {
        return new VoxelLayerBuilder();
    }

    @Override
    public void insertEntry(Vector3i pos, VoxelTreeEntry entry) {
        minVoxel.min(pos);
        maxVoxel.max(pos);

        super.insertEntry(pos, entry);
    }

    public @WeakValue BlockLayer build(BlockRegistry registry) {
        long hash = 1;
        VoxelLayer[] result = new VoxelLayer[ENTRIES_SIZE];

        for (int i = 0; i < ENTRIES_SIZE; i++) {
            hash = hash * 31;

            var entry = getEntry(i);
            if (entry == null) continue;

            var layer = ((VoxelLayerBuilder) entry).build(registry);
            result[i] = layer;

            hash+= layer.longHashCode();
        }

        minVoxel.min(VECTOR_15);
        maxVoxel.max(VECTOR_ZERO);

        Vector3i edgeLengths = maxVoxel.sub(minVoxel).max(VECTOR_ONE);
        return registry.allocateBlockLayer(
                result,
                size(),
                hash,
                edgeLengths.x * edgeLengths.y * edgeLengths.z
        );
    }


    @Override
    public void uploadTo(VoxelEntryMemory memory) {
        throw new UnsupportedOperationException("uploadTo");
    }

    private static final Vector3i VECTOR_ZERO = new Vector3i();
    private static final Vector3i VECTOR_ONE = new Vector3i(1);
    private static final Vector3i VECTOR_15 = new Vector3i(15);
}
