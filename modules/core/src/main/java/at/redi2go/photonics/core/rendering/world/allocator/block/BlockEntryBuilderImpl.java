package at.redi2go.photonics.core.rendering.world.allocator.block;

import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.RegionMapping;
import at.redi2go.photonics.core.rendering.world.RtVoxel;
import at.redi2go.photonics.core.rendering.world.allocator.BufferWorldAllocator;
import at.redi2go.photonics.core.rendering.world.allocator.PaletteAllocation;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.MutablePaletteEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteBuilder;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import at.redi2go.photonics.core.util.IntPacking;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jetbrains.annotations.Nullable;

public class BlockEntryBuilderImpl extends RegionMapping implements BlockEntry.Builder {
    private final BufferWorldAllocator allocator;
    private int skylight = 0;
    private final MutablePaletteEntry[] data;

    public BlockEntryBuilderImpl(BufferWorldAllocator allocator, RegionMapping regions) {
        super(regions);

        this.allocator = allocator;
        this.data = new MutablePaletteEntry[RtVoxel.ENTRIES_SIZE];
    }

    public BlockEntryBuilderImpl(BufferWorldAllocator allocator) {
        this.allocator = allocator;
        this.data = new MutablePaletteEntry[RtVoxel.ENTRIES_SIZE];
    }

    public void load(BlockEntryData entryData, ShortSet clearedRegions) {
        if (regionCount() == 0) {
            if (clearedRegions.contains(singleGetRegion()))
                return;
            else {
                clearedRegions = ShortSet.of();
            }
        }

        BlockVoxelImpl voxel = entryData.blockVoxel();
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

                    data[voxelIndex] = new MutablePaletteEntry(entryData.getPaletteEntry(entry));
                }

                sectionData >>= valueShift;
            }
        }
    }

    @Override
    public int begin() {
        throw new UnsupportedOperationException("begin");
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
    public boolean insert(
            int x,
            int y,
            int z,
            short region,
            int normal,
            TextureData textureData
    ) {
        int voxelIndex = VoxelModel.toVoxelIndex(x & 15, y & 15, z & 15);

        MutablePaletteEntry entry = data[voxelIndex];

        if (entry != null) {
            if (getRegion(voxelIndex) != region)
                return false;
        } else {
            entry = new MutablePaletteEntry();
            data[voxelIndex] = entry;

            setRegion(voxelIndex, region);
        }

        return entry.update(normal, textureData);
    }

    @Override
    public @Nullable Builder clearRegions(ShortSet regions) {
        throw new UnsupportedOperationException("clearRegions");
    }

    @Override
    public BlockEntry build() {
        var builder = new PaletteBuilder();
        for (int i = 0; i < RtVoxel.ENTRIES_SIZE; i++) {
            var entry = data[i];
            if (entry == null) continue;

            builder.add(entry);
        }

        var palette = builder.build();
        var regionBuilder = new RegionBuilder();

        // We need a bit to indicate transparency
        // Bit 0 is used so that it's unaffected by the int packing
        // Max element is <size> because index 0 is used for air
        var shift = IntPacking.shiftFactor(palette.size() << 1);

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
        }

        BlockVoxelImpl blockVoxel = allocator.allocateBlockVoxel(
                hash,
                shift,
                voxelData
        );

        PaletteAllocation[] paletteArray = new PaletteAllocation[palette.size()];
        for (int i = 0; i < palette.size(); i++)
            paletteArray[i] = allocator.allocatePalette(palette.get(i));

        var result = new BlockEntryImpl(
                regionBuilder,
                allocator.allocateBlockEntryData(
                        skylight,
                        paletteArray,
                        blockVoxel
                )
        );

        result.acquire();

        return result;
    }

    @Override
    public void close() {

    }

    private class RegionBuilder extends RegionMapping {
        public void set(int voxelIndex) {
            setRegion(
                    voxelIndex,
                    BlockEntryBuilderImpl.this.getRegion(voxelIndex)
            );
        }
    }
}
