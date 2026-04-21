package at.redi2go.photonics.core.rendering.world.registry.block;

import at.redi2go.photonics.core.rendering.world.RegionMapping;
import at.redi2go.photonics.core.rendering.world.RtVoxel;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.BlockPalette;
import at.redi2go.photonics.core.rendering.world.block.palette.MutablePaletteEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteBuilder;
import at.redi2go.photonics.core.rendering.world.registry.BufferBlockRegistry;
import at.redi2go.photonics.core.util.IntPacking;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractBlockBuilder extends RegionMapping implements BlockEntry.Builder {
    protected final BufferBlockRegistry registry;

    protected int skylight = 0;
    protected final MutablePaletteEntry[] data;

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
    public boolean load(AbstractBlockEntry<?> entryData, ShortSet clearedRegions) {
        int regionCount = regionCount();
        if (regionCount == 1) {
            if (clearedRegions.contains(singleGetRegion())) return true;

            clearedRegions = ShortSet.of();
        } else if (regionCount == 0) clearedRegions = ShortSet.of();

        boolean isEmpty = true;

        AbstractBlockVoxel voxel = entryData.blockVoxel();
        int shift = voxel.shift();

        int voxelDataSize = RtVoxel.ENTRIES_SIZE >> shift;
        int sectionLength = IntPacking.sectionLength(shift);

        int valueShift = 1 << IntPacking.valueShift(shift);
        int valueMask = IntPacking.valueMask(shift);

        var buffer = voxel.buffer();

        for (int o = 0; o < voxelDataSize; o++) {
            var sectionData = buffer.get(o);

            for (int i = 0; i < sectionLength; i++) {
                moveEntry :
                {
                    int entry = (sectionData & valueMask) >> 1;
                    if (entry == 0)
                        break moveEntry;

                    int voxelIndex = o + i;

                    if (!clearedRegions.isEmpty() && clearedRegions.contains(getRegion(voxelIndex)))
                        break moveEntry;

                    isEmpty = false;
                    data[voxelIndex] = new MutablePaletteEntry(entryData.getPaletteEntry(entry));
                }

                sectionData >>= valueShift;
            }
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
        // We need a bit to indicate transparency
        // Bit 0 is used so that it's unaffected by the int packing
        // Max element is <size> because index 0 is used for air

        //TODO: FIX ME!
        //var shift = IntPacking.shiftFactor(palette.size() << 1);
        var shift = IntPacking.shiftFactor(Integer.MAX_VALUE);

        var voxelData = new int[RtVoxel.ENTRIES_SIZE >> shift];
        var sectionLength = IntPacking.sectionLength(shift);

        long hash = 0;

        for (int o = 0; o < voxelData.length; o++) {
            int entry = 0;

            for (int i = 0; i < sectionLength; i++) {
                int voxelIndex = (o << shift) + i;

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

                entry = IntPacking.setValue(entry, i, voxelEntry, shift);
            }

            voxelData[o] = entry;
        }

        return new BuildResult(hash, shift, voxelData);
    }

    @Override
    public @Nullable Builder clearRegions(ShortSet regions) {
        throw new UnsupportedOperationException("clearRegions");
    }

    @Override
    public int begin() {
        throw new UnsupportedOperationException("begin");
    }

    @Override
    public void close() {
        //not used
    }

    protected  record BuildResult(long hash, int shift, int[] voxelData) {}

    protected interface RegionBuilder {
        void set(int voxelIndex);
    }
}
