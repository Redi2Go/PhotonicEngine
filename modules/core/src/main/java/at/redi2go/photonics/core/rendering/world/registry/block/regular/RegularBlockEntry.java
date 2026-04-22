package at.redi2go.photonics.core.rendering.world.registry.block.regular;

import at.redi2go.photonics.core.model.VoxelEntry;
import at.redi2go.photonics.core.rendering.world.RegionMapping;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.ContainedBlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jetbrains.annotations.Nullable;

public class RegularBlockEntry extends RegionMapping implements BlockEntry {
    private final RegularBlockVariant variant;

    public RegularBlockEntry(RegionMapping regions, RegularBlockVariant variant) {
        super(regions);

        this.variant = variant;
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
        var builder = new RegularBlockBuilder(variant.registry(), this);
        return builder.load(variant, regions) ? null : builder;
    }

    @Override
    public VoxelEntry toMutableEntry() {
        var builder = new RegularBlockBuilder(variant.registry(), this);
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
