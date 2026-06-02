package at.redi2go.photonics.core.rendering.world.registry.block;

import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryMemory;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeNode;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

public class BlockLayer extends BlockNodeObject {
    private final long hash;
    private final int boundingVolume;

    BlockLayer(
            @Nullable VoxelLayer[] entries,
            int size,
            long hash,
            int boundingVolume,
            BlockRegistry blockRegistry
    ) {
        super(
                blockRegistry,
                BLOCK_DEPTH,
                size,
                entries
        );

        this.hash = hash;
        this.boundingVolume = boundingVolume;
    }

    public int boundingVolume() {
        return boundingVolume;
    }

    @Override
    protected boolean useChildMask() {
        return true;
    }

    @Override
    protected int extraFieldCount() {
        return 0;
    }

    @Override
    public void uploadTo(VoxelEntryMemory memory) {
        memory.setEntryFlag(false);
        memory.setExtraFields(0);

        super.uploadTo(memory);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(hash);
    }

    @Override
    public boolean equals(Object o) {
        return o == this || (o instanceof BlockLayer other && other.hash == hash);
    }

    @Override
    protected VoxelTreeNode createNode(int x, int y, int z) {
        throw new UnsupportedOperationException("createNode");
    }
}
