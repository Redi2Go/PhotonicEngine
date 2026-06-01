package at.redi2go.photonics.core.rendering.world.tree.nodes;

import at.redi2go.photonics.core.rendering.world.WorldManager;
import at.redi2go.photonics.core.rendering.world.allocator.WorldAllocator;
import at.redi2go.photonics.core.rendering.world.tree.BlockMergeMode;
import org.joml.Vector3i;

public class ChunkNode extends WorldNode {
    ChunkNode(
            WorldManager worldManager,
            WorldAllocator allocator,
            BlockMergeMode mergeMode,
            Vector3i pos
    ) {
        super(worldManager, allocator, mergeMode, CHUNK_DEPTH, pos);

        worldManager.addChunk(this);
    }

    public void removeFromTree() {
        var parent = this.parent;
        if (parent == null) return;

        ((ChunkContainerNode) parent).removeAllChunks();
    }

    @Override
    public void close() {
        worldManager.removeChunk(this);

        super.close();
    }
}
