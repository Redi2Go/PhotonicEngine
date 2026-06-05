package at.redi2go.photonics.core.rendering.world.tree.entries;

import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryMemory;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.registry.block.model.BlockPartImpl;
import at.redi2go.photonics.core.rendering.world.registry.light.WorldLight;
import at.redi2go.photonics.core.rendering.world.registry.object.WeakValue;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeEntry;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

public record LightBlockEntry(
        int region,
        int skylight,
        @WeakValue WorldLight light,
        BlockPartImpl part
) implements BlockEntry {
    public LightBlockEntry {
        light.acquireReference();
    }

    @Override
    public int boundingVolume() {
        return part.blockLayer().boundingVolume();
    }

    @Override
    public IntSet regions() {
        return IntSet.of(region);
    }

    @Override
    public void uploadTo(VoxelEntryMemory memory) {
        part.blockLayer().uploadTo(memory);
        memory.setExtraFields(skylight, light.entryData());
    }

    @Override
    public @Nullable VoxelTreeEntry removeRegions(IntSet regions) {
        if (!regions.contains(region)) return this;

        close();
        return null;
    }

    @Override
    public BlockEntry merge(BlockEntry entry) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void close() {
        part.close();
        light.close();
    }
}
