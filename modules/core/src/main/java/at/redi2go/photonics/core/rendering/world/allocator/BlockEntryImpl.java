package at.redi2go.photonics.core.rendering.world.allocator;

import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.BlockVoxel;
import at.redi2go.photonics.core.rendering.world.block.palette.BlockPalette;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import java.nio.IntBuffer;

public class BlockEntryImpl extends AbstractRefCounter implements BlockEntry, HashedObject {
    private final MemoryView memory;

    private final BlockVoxel blockVoxel;
    private final PaletteAllocation[] palette;
    private final long hashCode;

    public BlockEntryImpl(
            BufferWorldAllocator allocator,
            int skylight,
            BlockPalette palette,
            BlockVoxel blockVoxel
    ) {
        super(allocator);

        memory = allocator.allocate(4 * (palette.size() + 3), this);
        IntBuffer buffer = memory.buffer().asIntBuffer();

        buffer.put(skylight);
        long hashCode = skylight;

        blockVoxel.acquire();
        this.palette = new PaletteAllocation[palette.size()];

        try {
            this.blockVoxel = blockVoxel;

            buffer.put(blockVoxel.memory());
            hashCode = hashCode * 31 + blockVoxel.memory();

            buffer.put(palette.size());
            hashCode = hashCode * 31 + palette.size();

            for (int i = 0; i < palette.size(); i++) {
                var entry = allocator.allocatePalette(palette.get(i));
                entry.acquire();

                this.palette[i] = entry;

                //Spin wait to wait for palette to be allocated, possible but unlikely
                while (!entry.isReady())
                    Thread.onSpinWait();

                buffer.put(entry.begin());

                hashCode = hashCode * 31 + entry.begin();
            }
        } catch (Throwable t) {
            blockVoxel.release();
            for (int i = 0; i < palette.size(); i++) {
                var entry = this.palette[i];
                if (entry == null) break;

                entry.release();
            }

            throw t;
        }

        this.hashCode = hashCode;
    }

    @Override
    public long hash() {
        return hashCode;
    }

    @Override
    public int memory() {
        return MemoryView.intBufferBegin(memory);
    }

    @Override
    public Builder createBuilder() {
       throw new NotImplementedException("TODO");
    }

    @Override
    public @Nullable Builder clearRegions(short[] regions) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void close() {
        memory.close();

        blockVoxel.release();
        for (PaletteAllocation entry : palette)
            entry.release();
    }
}
