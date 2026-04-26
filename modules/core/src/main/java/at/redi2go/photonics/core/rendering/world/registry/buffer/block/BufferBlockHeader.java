package at.redi2go.photonics.core.rendering.world.registry.buffer.block;

import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteEntry;
import at.redi2go.photonics.core.rendering.world.registry.buffer.BufferBlockRegistry;
import at.redi2go.photonics.core.rendering.world.registry.buffer.BufferObject;
import at.redi2go.photonics.core.rendering.world.registry.buffer.BufferPaletteObject;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.nio.IntBuffer;
import java.util.List;

public class BufferBlockHeader extends BufferObject<BufferBlockHeader, MemoryView> {
    private final ManagedRef<BufferBlockVoxel> blockVoxel;

    private final int skylight;
    private final List<ManagedRef<BufferPaletteObject.Entry>> palette;
    private final int[] tint;

    private final long hashCode;

    private final LongSet variants = new LongOpenHashSet();

    public BufferBlockHeader(
            BufferBlockRegistry registry,
            ManagedRef<BufferBlockVoxel> blockVoxel,
            int skylight,
            List<ManagedRef<BufferPaletteObject.Entry>> palette,
            int[] tint,
            long voxelHash,
            long tintHash
    ) {
        super(registry);

        this.blockVoxel = blockVoxel;
        this.skylight = skylight;
        this.palette = palette;
        this.tint = tint;

        long hashCode = skylight;
        hashCode = hashCode * 31 + voxelHash;
        hashCode = hashCode * 31 + tintHash;

        this.hashCode = hashCode;
    }

    @Override
    protected void loadDependants(List<ManagedRef<?>> output) {
        output.add(blockVoxel);
        output.addAll(palette);
    }

    @Override
    protected BufferBlockHeader getWrappedValue() {
        return this;
    }

    public void allocate() {
        int adjustedLength = Integer.highestOneBit(palette.size());
        if (adjustedLength != palette.size()) adjustedLength <<= 1;

        adjustedLength <<= 1;

        setMemory(registry.allocate(4 * (adjustedLength + 5)));
        IntBuffer buffer = memoryOrThrow().buffer().asIntBuffer();

        buffer.put(skylight);
        buffer.put(blockVoxel.get().begin());
        buffer.put(palette.size());
        for (int i = 0; i < palette.size(); i++) {
            buffer.put(tint[i]);

            var entry = palette.get(i);
            buffer.put(entry.get().begin());
        }

        memoryOrThrow().upload();
    }

    public int begin() {
        return MemoryView.intBufferBegin(memoryOrThrow());
    }

    public int skylight() {
        return skylight;
    }

    public BufferBlockRegistry registry() {
        return registry;
    }

    public BufferBlockVoxel blockVoxel() {
        return blockVoxel.get();
    }

    public PaletteEntry getPaletteEntry(int index) {
        return palette.get(index - 1).get();
    }

    public void addVariant(long variant) {
        variants.add(variant);
    }

    public int getTint(int index) {
        return tint[index - 1];
    }

    @Override
    public int hashCode() {
        return Long.hashCode(hashCode);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BufferBlockHeader other && hashCode == other.hashCode;
    }

    @Override
    protected boolean dispose() {
        var closed = super.dispose();
        if (closed) {
            for (var itr = variants.longIterator(); itr.hasNext(); )
                registry.removeVariantHeader(itr.nextLong(), this);
        }

        return closed;
    }

    public static long voxelHash(List<ManagedRef<BufferPaletteObject.Entry>> palette, BufferBlockVoxel blockVoxel) {
        long hashCode = palette.size();
        for (ManagedRef<BufferPaletteObject.Entry> ref : palette) {
            var entry = ref.get();

            entry.awaitAllocated();
            hashCode = hashCode * 31 + entry.begin();
        }

        blockVoxel.awaitAllocated();
        hashCode = hashCode * 31 + blockVoxel.begin();

        return hashCode;
    }

    public static long tintHash(int[] tints) {
        long hashCode = 0;
        for (int tint : tints)
            hashCode = hashCode * 31 + tint;

        return hashCode;
    }
}
