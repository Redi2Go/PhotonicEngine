package at.redi2go.photonics.core.rendering.world.tree.nodes;

import at.redi2go.photonics.core.rendering.world.WorldManager;
import at.redi2go.photonics.core.rendering.world.allocator.WorldAllocator;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.tree.BlockMergeMode;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeEntry;
import org.joml.Vector3i;

public class BlockContainerNode extends WorldNode {
    BlockContainerNode(
            WorldManager worldManager,
            WorldAllocator allocator,
            BlockMergeMode mergeMode,
            Vector3i pos
    ) {
        super(worldManager, allocator, mergeMode, BLOCK_CONTAINER_DEPTH, pos);
    }

    @Override
    public void insertEntry(int x, int y, int z, VoxelTreeEntry entry) {
        if (!(entry instanceof BlockEntry blockEntry))
            throw new IllegalArgumentException("only BlockEntry can be inserted to BlockContainerNode");

        containedRegions.addAll(blockEntry.regions());

        int index = indexOf(x, y, z, magnitude());

        final BlockEntry oldEntry = (BlockEntry) getEntry(index);
        final BlockEntry newEntry = oldEntry == null ? blockEntry : mergeMode.merge(oldEntry, blockEntry);

        var ignored = replaceEntry(index, newEntry);
    }
}
