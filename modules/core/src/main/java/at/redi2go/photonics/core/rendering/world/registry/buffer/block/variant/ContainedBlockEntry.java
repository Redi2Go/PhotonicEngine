package at.redi2go.photonics.core.rendering.world.registry.buffer.block.variant;

import at.redi2go.photonics.core.model.VoxelEntry;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import at.redi2go.photonics.core.rendering.world.registry.buffer.BufferObject;
import at.redi2go.photonics.core.rendering.world.registry.buffer.block.BufferBlockEntry;
import at.redi2go.photonics.core.rendering.world.registry.buffer.block.BufferBlockHeader;
import at.redi2go.photonics.core.rendering.world.registry.buffer.block.BufferBlockVoxel;
import at.redi2go.photonics.core.rendering.world.registry.buffer.block.regular.RegularBlockBuilder;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

public class ContainedBlockEntry implements BufferBlockEntry {
    private final short region;

    private final BufferObject.Ref<BufferBlockHeader> header;
    private final BufferObject.Ref<BufferBlockVoxel.Variant> variant;

    public ContainedBlockEntry(
            short region,
            @NonNls BufferObject.Ref<BufferBlockHeader> header,
            @NonNls BufferObject.Ref<BufferBlockVoxel.Variant> variant
    ) {
        super();
        this.region = region;

        this.header = header;
        this.variant = variant;
    }

    @Override
    public BufferBlockHeader header() {
        return header.get();
    }

    @Override
    public int skylight() {
        return header().skylight();
    }

    @Override
    public int entryData() {
        return header().begin();
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
        var builder = new RegularBlockBuilder(header().registry());
        builder.initRegion(region);
        builder.load(header(), ShortSet.of());

        return builder;
    }

    @Override
    public @Nullable VoxelEntry build() {
        return this;
    }

    @Override
    public void close() {
        header.close();
        variant.close();
    }
}
