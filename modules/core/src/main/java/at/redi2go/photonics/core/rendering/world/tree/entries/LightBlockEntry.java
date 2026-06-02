package at.redi2go.photonics.core.rendering.world.tree.entries;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryMemory;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.registry.light.WorldLight;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeEntry;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.Nullable;

public record LightBlockEntry(
        BlockEntry block,
        WorldLight light
) implements BlockEntry {
    @Override
    public int boundingVolume() {
        return block.boundingVolume();
    }

    @Override
    public IntSet regions() {
        return block.regions();
    }

    @Override
    public @Nullable VoxelTreeEntry removeRegions(IntSet regions) {
        var result = block.removeRegions(regions);

        if (result == null) {
            light.close();
            return null;
        }

        if (result == block) return this;

        return new LightBlockEntry(
                block,
                light
        );
    }

    @Override
    public BlockEntry merge(BlockEntry entry) {
        return new LightBlockEntry(
                block.merge(entry),
                light
        );
    }

    @Override
    public void uploadTo(VoxelEntryMemory memory) {
        block.uploadTo(memory);
        memory.setExtraFields(light.entryData());
    }

    @Override
    public void close() {
        block.close();
        light.close();
    }
}
