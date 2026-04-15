package at.redi2go.photonics.core.rendering.world.block;

//public class MutableBlockEntry implements BlockEntry {
//    private final TempSlot slot;
//    private final WorldAllocator allocator;
//
//    private final MutablePaletteEntry[] data;
//    private final short[] regions;
//
//    public MutableBlockEntry(WorldAllocator allocator) {
//        this.slot = allocator.createTemp(this);
//        this.allocator = allocator;
//
//        this.data = new MutablePaletteEntry[RtVoxel.ENTRIES_SIZE];
//        this.regions = new short[RtVoxel.ENTRIES_SIZE];
//    }
//
//    @Override
//    public int memory() {
//        return slot.id();
//    }
//
//    @Override
//    public MutableBlockEntry createMutable() {
//        return this;
//    }
//
//    public boolean insert(
//            int x, int y, int z,
//            int region,
//            int normal,
//            TextureData textureData
//    ) {
//        var index = VoxelModel.toVoxelIndex(
//                x & 15,
//                y & 15,
//                z & 15
//        );
//
//        MutablePaletteEntry entry = data[index];
//        if (entry != null) {
//            if (regions[index] != region)
//                return false;
//        } else {
//            entry = new MutablePaletteEntry();
//            data[index] = entry;
//        }
//
//        return entry.update(normal, textureData);
//    }
//
//    public BlockEntry build() {
//        var builder = Palette.builder();
//
//        for (int i = 0; i < RtVoxel.ENTRIES_SIZE; i++) {
//            var entry = data[i];
//            if (entry == null) continue;
//
//            builder.add(entry);
//        }
//
//        var palette = builder.build();
//
//        var shift = palette.size() > (Byte.MAX_VALUE - 1) ? 3 : 4;
//
//        TODO: use zero as transparent bit instead
//        var transparentBit = 1 << ((1 << shift) - 1);
//
//        var voxelData = new int[RtVoxel.ENTRIES_SIZE >> shift];
//        var voxelPerInt = 32 >> shift;
///
//        long hash = 0;
//
//        for (int o = 0; o < voxelData.length; o++) {
//            int offset = o << shift;
//            int entry = 0;
//
//            for (int i = 0; i < voxelPerInt; i++) {
//                var paletteEntry = data[offset + i];
//                var voxelEntry = palette.getIndex(paletteEntry);
//
//                hash = hash * 31 + ((long) voxelEntry << 15 | (offset + i));
//
//                if (paletteEntry != null && paletteEntry.hasTransparentFace())
//                    voxelEntry |= transparentBit;
//
//                entry = entry | (voxelEntry << (i << shift));
//            }
//        }
//
//
//    }
//
//    public void close() {
//        slot.close();
//    }
//}

public class MutableBlockEntryOld {

}

