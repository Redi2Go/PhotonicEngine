package at.redi2go.photonics.core.rendering.world.registry.block.contained;

import at.redi2go.photonics.core.rendering.world.registry.BufferBlockRegistry;
import at.redi2go.photonics.core.rendering.world.registry.PaletteAllocation;
import at.redi2go.photonics.core.rendering.world.registry.block.AbstractBlockEntry;

public class ContainedBlockVariant extends AbstractBlockEntry<ContainedBlockVoxelImpl> {
    public ContainedBlockVariant(
            BufferBlockRegistry registry,
            ContainedBlockVoxelImpl blockVoxel,
            int shift,
            int valueMask,
            int skylight,
            PaletteAllocation[] palette,
            int[] tint
    ) {
        super(registry, blockVoxel, shift, valueMask, skylight, palette, tint);
    }
}
