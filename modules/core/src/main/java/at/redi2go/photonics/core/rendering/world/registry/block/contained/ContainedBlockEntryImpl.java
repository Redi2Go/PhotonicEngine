package at.redi2go.photonics.core.rendering.world.registry.block.contained;

import at.redi2go.photonics.core.model.VoxelEntry;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.ContainedBlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import at.redi2go.photonics.core.rendering.world.registry.block.regular.RegularBlockBuilder;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jetbrains.annotations.Nullable;

public class ContainedBlockEntryImpl implements ContainedBlockEntry {
    private final short region;
    private final ContainedBlockVariant variant;

    public ContainedBlockEntryImpl(short region, ContainedBlockVariant variant) {
        this.region = region;
        this.variant = variant;

        variant.acquire();
    }

    @Override
    public int skylight() {
        return variant.skylight();
    }

    @Override
    public int entryData() {
        variant.awaitAllocated();
        return variant.begin();
    }

    @Override
    public void insertVoxel(int x, int y, int z, short region, int normal, int tint, TextureData textureData) {
        throw new UnsupportedOperationException("insertVoxel");
    }

    @Override
    public void insertBlock(int x, int y, int z, short region, BlockEntry block) {
        throw new UnsupportedOperationException("insertBlock");
    }

    @Override
    public @Nullable VoxelEntry removeRegions(ShortSet regions) {
        if (regions.contains(region)) return null;

        return toMutableEntry();
    }

    @Override
    public VoxelEntry toMutableEntry() {
        var builder = new RegularBlockBuilder(variant.registry());
        builder.initRegion(region);
        builder.load(variant, ShortSet.of());

        return builder;
    }

    @Override
    public @Nullable VoxelEntry build() {
        return this;
    }

    @Override
    public void close() {
        variant.close();
    }
}
