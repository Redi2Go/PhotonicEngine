package at.redi2go.photonics.core.rendering.world.registry.block.contained;

import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.ContainedBlockEntry;
import at.redi2go.photonics.core.rendering.world.block.ContainedBlockVoxel;
import at.redi2go.photonics.core.rendering.world.registry.BufferBlockRegistry;
import at.redi2go.photonics.core.rendering.world.registry.PaletteAllocation;
import at.redi2go.photonics.core.rendering.world.registry.block.AbstractBlockEntry;
import at.redi2go.photonics.core.rendering.world.registry.block.AbstractBlockVoxel;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jetbrains.annotations.Nullable;

public class ContainedBlockVoxelImpl extends AbstractBlockVoxel implements ContainedBlockVoxel {
    private final long vertexHash;
    private final int valueMask;

    private boolean voxelHashReady = false;
    private long voxelHash = 0;

    /**
     * Maps palette indexes to their index in the tint array for createVariant
     */
    private final int[] tintMappings;
    private final PaletteAllocation[] palette;

    public ContainedBlockVoxelImpl(
            BufferBlockRegistry registry,
            int shift,
            int valueMask,
            long hashCode,
            long vertexHash,
            int[] tintMappings,
            PaletteAllocation[] palette
    ) {
        super(registry, shift, hashCode);

        this.vertexHash = vertexHash;
        this.valueMask = valueMask;

        this.tintMappings = tintMappings;
        this.palette = palette;
    }

    @Override
    public void allocate(int[] data) {
        super.allocate(data);

        for (var entry : palette)
            entry.acquire();
    }

    private long getVoxelHash() {
        if (voxelHashReady) return voxelHash;

        voxelHash = AbstractBlockEntry.voxelHash(palette, this);
        voxelHashReady = true;
        return voxelHash;
    }

    @Override
    public ContainedBlockEntry createVariant(
            IntArraySet tint,
            int skylight,
            short region
    ) {
        int[] tintArray = tint.toIntArray();

        long tintHash = 0;
        int[] paletteTint = new int[palette.length];

        for (int i = 0; i < palette.length; i++) {
            int tintValue = tintArray[tintMappings[i]];

            tintHash = tintHash * 31 + tintValue;
            paletteTint[i] = tintValue;
        }

        return new ContainedBlockEntryImpl(
                region,
                registry.allocateContainedBlockVariant(
                        this,
                        shift,
                        valueMask,
                        skylight,
                        palette,
                        paletteTint,
                        getVoxelHash(),
                        tintHash
                )
        );
    }

    @Override
    protected void dispose() {
        registry.freeContainedBlock(vertexHash);

        for (var entry : palette)
            entry.close();
    }
}
