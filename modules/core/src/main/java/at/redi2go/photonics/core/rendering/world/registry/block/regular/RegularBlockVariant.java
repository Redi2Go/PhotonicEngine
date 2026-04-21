package at.redi2go.photonics.core.rendering.world.registry.block.regular;

import at.redi2go.photonics.core.rendering.world.registry.BufferBlockRegistry;
import at.redi2go.photonics.core.rendering.world.registry.PaletteAllocation;
import at.redi2go.photonics.core.rendering.world.registry.block.AbstractBlockEntry;

public class RegularBlockVariant extends AbstractBlockEntry<RegularBlockVoxel> {
    public RegularBlockVariant(
            BufferBlockRegistry registry,
            RegularBlockVoxel blockVoxel,
            int shift,
            int valueMask,
            int skylight,
            PaletteAllocation[] palette,
            int[] tint
    ) {
        super(registry, blockVoxel, shift, valueMask, skylight, palette, tint);
    }
}
