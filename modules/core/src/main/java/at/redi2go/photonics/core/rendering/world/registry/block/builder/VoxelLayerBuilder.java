package at.redi2go.photonics.core.rendering.world.registry.block.builder;

import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryMemory;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockRegistry;
import at.redi2go.photonics.core.rendering.world.registry.block.VoxelLayer;
import at.redi2go.photonics.core.rendering.world.registry.object.WeakValue;
import at.redi2go.photonics.core.rendering.world.registry.palete.MutablePaletteEntry;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeEntry;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeNode;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

public class VoxelLayerBuilder extends VoxelTreeNode {
    public VoxelLayerBuilder() {
        super(VOXEL_DEPTH);
    }

    @Override
    public VoxelTreeEntry getEntry(int index) {
        return super.getEntry(index);
    }

    @Override
    public void insertEntry(int x, int y, int z, VoxelTreeEntry entry) {
        int index = indexOf(x, y, z, magnitude());

        var oldEntry = getEntry(index);
        var accumulator = MutablePaletteEntry.copyOf(oldEntry);

        accumulator.update(entry);

        setEntry(index, accumulator);
    }

    public @WeakValue VoxelLayer build(BlockRegistry registry) {
        return registry.allocateVoxelLayer(this);
    }

    @Override
    protected VoxelTreeNode createNode(int x, int y, int z) {
        throw new UnsupportedOperationException("createNode");
    }

    @Override
    public void uploadTo(VoxelEntryMemory memory) {
        throw new UnsupportedOperationException("uploadTo");
    }
}
