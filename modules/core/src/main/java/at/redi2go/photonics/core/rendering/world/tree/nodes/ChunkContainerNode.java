package at.redi2go.photonics.core.rendering.world.tree.nodes;

import at.redi2go.photonics.core.rendering.world.WorldManager;
import at.redi2go.photonics.core.rendering.world.allocator.WorldAllocator;
import at.redi2go.photonics.core.rendering.world.tree.BlockMergeMode;
import org.joml.Vector3i;

public class ChunkContainerNode extends WorldNode {
    ChunkContainerNode(
            WorldManager worldManager,
            WorldAllocator allocator,
            BlockMergeMode mergeMode,
            Vector3i pos
    ) {
        super(worldManager, allocator, mergeMode, CHUNK_CONTAINER_DEPTH, pos);
    }

    public void removeAllChunks() {
        if (isEmpty()) return;

        for (int i = 0; i < ENTRIES_SIZE; i++) {
            var entry = replaceEntry(i, null);

            if (entry != null)
                ((ChunkNode) entry).parent = null;
        }
    }
}
