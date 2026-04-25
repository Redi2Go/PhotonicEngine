package at.redi2go.photonics.core.rendering.world.registry.block.regular;

import at.redi2go.photonics.core.model.VoxelEntry;
import at.redi2go.photonics.core.rendering.world.RegionMapping;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import at.redi2go.photonics.core.rendering.world.registry.block.BufferBlockEntry;
import at.redi2go.photonics.core.rendering.world.registry.block.BufferBlockHeader;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jetbrains.annotations.Nullable;

public class RegularBlockEntry extends RegionMapping implements BufferBlockEntry {
    private final BufferBlockHeader header;

    public RegularBlockEntry(RegionMapping regions, BufferBlockHeader header) {
        super(regions);

        this.header = header;
        header.acquire();
    }

    @Override
    public BufferBlockHeader header() {
        return header;
    }

    @Override
    public int skylight() {
        return header.skylight();
    }

    @Override
    public int entryData() {
        return header.begin();
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
        var builder = new RegularBlockBuilder(header.registry(), this);
        return builder.load(header, regions) ? null : builder;
    }

    @Override
    public VoxelEntry toMutableEntry() {
        var builder = new RegularBlockBuilder(header.registry(), this);
        builder.load(header, ShortSet.of());

        return builder;
    }

    @Override
    public @Nullable VoxelEntry build() {
        return null;
    }

    @Override
    public void close() {

    }
}
