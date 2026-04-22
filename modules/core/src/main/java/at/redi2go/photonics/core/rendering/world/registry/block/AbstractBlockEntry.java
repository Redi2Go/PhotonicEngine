package at.redi2go.photonics.core.rendering.world.registry.block;

import at.redi2go.photonics.core.rendering.world.block.palette.PaletteEntry;
import at.redi2go.photonics.core.rendering.world.registry.AbstractHashedObject;
import at.redi2go.photonics.core.rendering.world.registry.BufferBlockRegistry;
import at.redi2go.photonics.core.rendering.world.registry.PaletteAllocation;
import it.unimi.dsi.fastutil.longs.LongLongPair;

import java.nio.IntBuffer;

public abstract class AbstractBlockEntry<T extends AbstractBlockVoxel> extends AbstractHashedObject {
    private final T blockVoxel;

    private final int shift;
    private final int valueMask;
    private final int skylight;
    private final PaletteAllocation[] palette;
    private final int[] tint;

    private final long hashCode;

    public AbstractBlockEntry(
            BufferBlockRegistry registry,
            T blockVoxel,
            int shift,
            int valueMask,
            int skylight,
            PaletteAllocation[] palette,
            int[] tint,
            long voxelHash,
            long tintHash
    ) {
        super(registry);

        this.blockVoxel = blockVoxel;
        this.shift = shift;
        this.valueMask = valueMask;
        this.skylight = skylight;
        this.palette = palette;
        this.tint = tint;

        long hashCode = skylight;
        hashCode = hashCode * 31 + voxelHash;
        hashCode= hashCode * 31 + tintHash;

        this.hashCode = hashCode;
    }

    public static long voxelHash(PaletteAllocation[] palette, AbstractBlockVoxel blockVoxel) {
        long hashCode = palette.length;
        for (int i = 0; i < palette.length; i++) {
            var entry = palette[i];

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

    @Override
    protected long hash() {
        return hashCode;
    }

    public void allocate() {
        int adjustedLength = Integer.highestOneBit(palette.length);
        if (adjustedLength != palette.length) adjustedLength <<= 1;

        adjustedLength <<= 1;

        initMemory(4 * (adjustedLength + 5));
        IntBuffer buffer = memory.buffer().asIntBuffer();

        blockVoxel.acquire();

        buffer.put(skylight);
        buffer.put(blockVoxel.begin());
        buffer.put(shift);
        buffer.put(valueMask);
        buffer.put(palette.length);
        for (int i = 0; i < palette.length; i++) {
            buffer.put(tint[i]);

            var entry = palette[i];
            entry.acquire();
            buffer.put(entry.begin());
        }

        memory.upload();
    }

    public int skylight() {
        return skylight;
    }

    public BufferBlockRegistry registry() {
        return registry;
    }

    public T blockVoxel() {
        return blockVoxel;
    }

    public PaletteEntry getPaletteEntry(int index) {
        return palette[index - 1];
    }

    public int getTint(int index) {
        return tint[index - 1];
    }

    @Override
    protected void dispose() {
        blockVoxel.close();
        for (var entry : palette)
            entry.close();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AbstractBlockEntry<?> other && hashCode == other.hashCode;
    }
}
