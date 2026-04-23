package at.redi2go.photonics.core.rendering.world.registry.block.regular;

import at.redi2go.photonics.core.rendering.world.registry.BufferBlockRegistry;
import at.redi2go.photonics.core.rendering.world.registry.PaletteAllocation;
import at.redi2go.photonics.core.rendering.world.registry.block.AbstractBlockEntry;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.longs.LongLongPair;

public class RegularBlockVariant extends AbstractBlockEntry<RegularBlockVoxel> {
    private RegularBlockVariant(
            BufferBlockRegistry registry,
            RegularBlockVoxel blockVoxel,
            int skylight,
            PaletteAllocation[] palette,
            int[] tint,
            LongLongPair hashes
    ) {
        super(
                registry,
                blockVoxel,
                skylight,
                palette,
                tint,
                hashes.secondLong(),
                hashes.firstLong()
        );
    }

    public RegularBlockVariant(
            BufferBlockRegistry registry,
            RegularBlockVoxel blockVoxel,
            int skylight,
            PaletteAllocation[] palette,
            int[] tint
    ) {
        this(
                registry,
                blockVoxel,
                skylight,
                palette,
                tint,
                LongLongPair.of(
                        tintHash(tint),
                        voxelHash(palette, blockVoxel)
                )
        );
    }
}
