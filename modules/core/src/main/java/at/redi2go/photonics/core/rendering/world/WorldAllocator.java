package at.redi2go.photonics.core.rendering.world;

import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.BlockVoxel;
import at.redi2go.photonics.core.rendering.world.block.palette.BlockPalette;
import at.redi2go.photonics.core.rendering.world.block.palette.Palette;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface WorldAllocator {
    /**
     * Reserves {@code byteSize} bytes for {@code object} and returns the allocated memory
     */
    MemoryView allocate(int byteSize, Object object);

    BlockEntry.Builder createBlockBuilder();

    /**
     * Returns the object that owns the memory at {@code begin}
     */
    @Nullable Object getOwner(int begin);
}
