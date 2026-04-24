package at.redi2go.photonics.core.rendering.world.registry.block;

import at.redi2go.photonics.core.rendering.world.block.palette.PaletteEntry;
import at.redi2go.photonics.core.rendering.world.registry.AbstractBufferObject;
import at.redi2go.photonics.core.rendering.world.registry.BufferBlockRegistry;
import at.redi2go.photonics.core.rendering.world.registry.PaletteAllocation;

import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Set;

public class BufferBlockHeader extends AbstractBufferObject {
    private final BufferBlockVoxel blockVoxel;
    private Set<BufferBlockVoxel.Variant> variants;

    private final int skylight;
    private final PaletteAllocation[] palette;
    private final int[] tint;

    private final long hashCode;

    public BufferBlockHeader(
            BufferBlockRegistry registry,
            BufferBlockVoxel blockVoxel,
            int skylight,
            PaletteAllocation[] palette,
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

    public void addVariant(BufferBlockVoxel.Variant variant) {
        synchronized (this) {
            if (!isOpen) return;

            var variants = this.variants;
            if (variants == null) {
                variants = new HashSet<>();
                this.variants = variants;
            }

            if (variants.add(variant))
                variant.acquire();
        }
    }

    public static long voxelHash(PaletteAllocation[] palette, BufferBlockVoxel blockVoxel) {
        long hashCode = palette.length;
        for (PaletteAllocation entry : palette) {
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

    public void allocate() {
        int adjustedLength = Integer.highestOneBit(palette.length);
        if (adjustedLength != palette.length) adjustedLength <<= 1;

        adjustedLength <<= 1;

        initMemory(4 * (adjustedLength + 5));
        IntBuffer buffer = memory.buffer().asIntBuffer();

        blockVoxel.acquire();

        buffer.put(skylight);
        buffer.put(blockVoxel.begin());
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

    public BufferBlockVoxel blockVoxel() {
        return blockVoxel;
    }

    public PaletteEntry getPaletteEntry(int index) {
        return palette[index - 1];
    }

    public int getTint(int index) {
        return tint[index - 1];
    }

    @Override
    protected long hash() {
        return hashCode;
    }

    @Override
    protected void dispose() {
        super.dispose();

        blockVoxel.close();
        for (var entry : palette) entry.close();

        synchronized (this) {
            for (var variant : variants)
                variant.close();
        }
    }
}
