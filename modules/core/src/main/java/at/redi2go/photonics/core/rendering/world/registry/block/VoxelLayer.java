package at.redi2go.photonics.core.rendering.world.registry.block;

import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryMemory;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteEntry;
import at.redi2go.photonics.core.rendering.world.registry.palete.PaletteRegistry;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeEntry;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeNode;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

public class VoxelLayer extends BlockNodeObject {
    private long hash;
    private long childMask = 0;

    VoxelLayer(
            @Nullable VoxelTreeEntry[] entries,
            PaletteRegistry paletteRegistry,
            BlockRegistry blockRegistry
    ) {
        super(blockRegistry, VOXEL_DEPTH);

        long hash = 1;

        for (int i = 0; i < ENTRIES_SIZE; i++) {
            hash = hash * 31;

            var entry = entries[i];
            if (entry == null) continue;

            data[i] = paletteRegistry.allocate((PaletteEntry) entry);
            size++;

            hash+= entry.hashCode();
        }

        this.hash = hash;
    }

    public long longHashCode() {
        return hash;
    }

    @Override
    protected boolean useChildMask() {
        return false;
    }

    @Override
    protected int extraFieldCount() {
        return 0;
    }

    @Override
    public void uploadTo(VoxelEntryMemory memory) {
         memory.setEntryFlag(true);
         super.uploadTo(memory);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(hash);
    }

    @Override
    public boolean equals(Object o) {
        return o == this || (o instanceof VoxelLayer other && other.hash == hash);
    }

    @Override
    protected VoxelTreeNode createNode(Vector3i pos) {
        throw new UnsupportedOperationException("createNode");
    }
}
