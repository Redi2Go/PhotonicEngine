package at.redi2go.photonics.core.rendering.world.registry.block.contained;

import at.redi2go.photonics.core.rendering.world.registry.BufferBlockRegistry;
import at.redi2go.photonics.core.rendering.world.registry.PaletteAllocation;
import at.redi2go.photonics.core.rendering.world.registry.block.AbstractBlockEntry;

public class ContainedBlockVariant extends AbstractBlockEntry<ContainedBlockVoxelImpl> {
    public ContainedBlockVariant(
            BufferBlockRegistry registry,
            ContainedBlockVoxelImpl blockVoxel,
            int skylight,
            PaletteAllocation[] palette,
            int[] tint,
            long voxelHash,
            long tintHash
    ) {
        super(registry, blockVoxel, skylight, palette, tint, voxelHash, tintHash);
    }
}
