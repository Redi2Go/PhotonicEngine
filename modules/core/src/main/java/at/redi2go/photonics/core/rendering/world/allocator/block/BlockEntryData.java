package at.redi2go.photonics.core.rendering.world.allocator.block;

import at.redi2go.photonics.core.rendering.world.allocator.AbstractHashedObject;
import at.redi2go.photonics.core.rendering.world.allocator.BufferWorldAllocator;
import at.redi2go.photonics.core.rendering.world.allocator.PaletteAllocation;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteEntry;

import java.nio.IntBuffer;

public class BlockEntryData extends AbstractHashedObject {
    private final int skylight;
    private final int shift;
    private final int valueMask;
    private final PaletteAllocation[] palette;
    private final BlockVoxelImpl blockVoxel;

    private final long hashCode;

    public BlockEntryData(
            BufferWorldAllocator allocator,
            int skylight,
            int shift,
            int valueMask,
            PaletteAllocation[] palette,
            BlockVoxelImpl blockVoxel
    ) {
        super(allocator);

        this.skylight = skylight;
        this.shift = shift;
        this.valueMask = valueMask;
        this.palette = palette;
        this.blockVoxel = blockVoxel;

        long hashCode = skylight;

        hashCode = hashCode * 31 + palette.length;
        for (PaletteAllocation entry : palette) {
            entry.awaitAllocated();
            hashCode = hashCode * 31 + entry.begin();
        }

        blockVoxel.awaitAllocated();
        hashCode = hashCode * 31 + blockVoxel.begin();

        this.hashCode = hashCode;
    }

    @Override
    protected long hash() {
        return hashCode;
    }

    public void allocate() {
        initMemory(4 * (palette.length + 5));
        IntBuffer buffer = memory.buffer().asIntBuffer();

        blockVoxel.acquire();

        buffer.put(skylight);
        buffer.put(blockVoxel.begin());
        buffer.put(shift);
        buffer.put(valueMask);
        buffer.put(palette.length);
        for (PaletteAllocation entry : palette) {
            entry.acquire();
            buffer.put(entry.begin());
        }

        memory.upload();
    }

    public BufferWorldAllocator allocator() {
        return allocator;
    }

    public int skylight() {
        return skylight;
    }

    public BlockVoxelImpl blockVoxel() {
        return blockVoxel;
    }

    public PaletteEntry getPaletteEntry(int index) {
        return palette[index - 1];
    }

    @Override
    protected void dispose() {
        blockVoxel.close();
        for (PaletteAllocation entry : palette)
            entry.close();
    }
}
