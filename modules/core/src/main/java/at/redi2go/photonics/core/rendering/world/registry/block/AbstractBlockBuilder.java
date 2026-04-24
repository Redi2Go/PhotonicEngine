package at.redi2go.photonics.core.rendering.world.registry.block;

import at.redi2go.photonics.core.model.VoxelEntry;
import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.RegionMapping;
import at.redi2go.photonics.core.rendering.world.RtVoxel;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.BlockPalette;
import at.redi2go.photonics.core.rendering.world.block.palette.MutablePaletteEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteBuilder;
import at.redi2go.photonics.core.rendering.world.registry.BufferBlockRegistry;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractBlockBuilder extends RegionMapping implements BlockEntry.Builder {
    protected final BufferBlockRegistry registry;

    protected int skylight = 0;
    protected final MutablePaletteEntry[] data;
    protected boolean isEmpty = true;

    public AbstractBlockBuilder(BufferBlockRegistry registry, RegionMapping mapping) {
        super(mapping);

        this.registry = registry;
        this.data = new MutablePaletteEntry[RtVoxel.ENTRIES_SIZE];
    }

    public AbstractBlockBuilder(BufferBlockRegistry registry) {
        this.registry = registry;
        this.data = new MutablePaletteEntry[RtVoxel.ENTRIES_SIZE];
    }

    public void initRegion(short region) {
        setRegion(0, region);
    }

    /**
     * Loads the voxel data of {@code entryData} into this builder, ignoring voxels in {@code clearedRegions}.
     *
     * @return {@code true} if this builder is empty
     */
    public boolean load(BufferBlockHeader entryData, ShortSet clearedRegions) {
        if (!entryData.isAllocated()) throw new IllegalStateException();

        int regionCount = regionCount();
        if (regionCount == 1) {
            if (clearedRegions.contains(singleGetRegion())) return true;

            clearedRegions = ShortSet.of();
        } else if (regionCount == 0) clearedRegions = ShortSet.of();

        BufferBlockVoxel voxel = entryData.blockVoxel();
        var buffer = voxel.buffer();

        for (int voxelIndex = 0; voxelIndex < RtVoxel.ENTRIES_SIZE; voxelIndex++) {
            int entry = buffer.get(voxelIndex);
            if (VoxelEntry.isAir(entry)) continue;

            if (!clearedRegions.isEmpty() && clearedRegions.contains(getRegion(voxelIndex))) continue;

            entry = VoxelEntry.getData(entry) >> 1;

            isEmpty = false;
            data[voxelIndex] = new MutablePaletteEntry(
                    entryData.getPaletteEntry(entry),
                    entryData.getTint(entry)
            );
        }

        return isEmpty;
    }


    @Override
    public int skylight() {
        return skylight;
    }

    @Override
    public void setSkylight(int skylight) {
        this.skylight = skylight;
    }

    @Override
    public int entryData() {
        throw new UnsupportedOperationException("entryData");
    }

    @Override
    public void insertBlock(int x, int y, int z, short region, BlockEntry block) {
        throw new UnsupportedOperationException("insertBlock");
    }

    @Override
    public @Nullable VoxelEntry removeRegions(ShortSet regions) {
        throw new UnsupportedOperationException("removeRegions");
    }

    @Override
    public VoxelEntry toMutableEntry() {
        return this;
    }

    protected BlockPalette buildPalette() {
        var builder = new PaletteBuilder();
        for (int i = 0; i < RtVoxel.ENTRIES_SIZE; i++) {
            var entry = data[i];
            if (entry == null) continue;

            builder.add(entry);
        }

        return builder.build();
    }

    protected BuildResult buildBlockVoxel(BlockPalette palette, RegionBuilder regionBuilder) {
        var voxelData = new int[RtVoxel.ENTRIES_SIZE];

        long hash = 0;
        for (int voxelIndex = 0; voxelIndex < RtVoxel.ENTRIES_SIZE; voxelIndex++) {
            var paletteEntry = data[voxelIndex];
            var voxelEntry = palette.getIndex(paletteEntry) << 1;

            hash = hash * 31 + (((long) voxelEntry << 14) | voxelIndex);

            handleEntry: {
                if (paletteEntry == null) break handleEntry;

                if (paletteEntry.hasTransparentFace())
                    voxelEntry |= 1;

                // TODO: Maybe? passive shrinkage
                regionBuilder.set(voxelIndex);
            }

            voxelData[voxelIndex] = paletteEntry == null ? VoxelModel.makeAirEntry(voxelIndex) : VoxelEntry.toData(voxelEntry);
        }

        return new BuildResult(hash, voxelData);
    }

    @Override
    public void close() {
        //not used
    }

    protected  record BuildResult(long hash, int[] voxelData) {}

    protected interface RegionBuilder {
        void set(int voxelIndex);
    }
}
