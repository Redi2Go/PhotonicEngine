package at.redi2go.photonics.core.rendering.world.registry.block.template;

import at.redi2go.photonics.core.rendering.world.block.palette.PaletteEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.TintBuilder;
import at.redi2go.photonics.core.rendering.world.registry.PaletteObject;
import at.redi2go.photonics.core.rendering.world.registry.WorldRegistry;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockHeader;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockVoxel;
import at.redi2go.photonics.core.rendering.world.registry.objects.WorldObject;
import org.joml.Vector3i;

import java.util.List;

public class BlockPartTemplate {
    private final Vector3i offset;
    private final int boundingVolume;

    private final int[] tintMapping;

    private final List<PaletteObject> palette;
    private final BlockVoxel blockVoxel;

    private final long voxelHash;

    public BlockPartTemplate(
            Vector3i offset,
            int boundingVolume,
            int[] tintMapping,
            List<PaletteObject> weakPalette,
            BlockVoxel weakBlockVoxel
    ) {
        this.offset = offset;
        this.boundingVolume = boundingVolume;

        this.tintMapping = tintMapping;
        this.palette = weakPalette;
        this.blockVoxel = weakBlockVoxel;

        this.voxelHash = BlockHeader.voxelHash(palette, blockVoxel);
    }

    void loadDependants(List<WorldObject<?>> output) {
        output.addAll(palette);
        output.add(blockVoxel);
    }

    public Vector3i offset() {
        return offset;
    }

    public int boundingVolume() {
        return boundingVolume;
    }

    public BlockHeader createVariantWeak(WorldRegistry worldRegistry, TintBuilder.Result tint) {
        int[] tintValues = new int[palette.size()];
        int[] blockTint = tint.tints().toIntArray();

        for (int i = 0; i < tintValues.length; i++)
            tintValues[i] = blockTint[tintMapping[i]];

        return worldRegistry.allocateBlockHeaderWeak(
                tintValues,
                palette,
                blockVoxel,
                voxelHash,
                BlockHeader.tintHash(tintValues)
        );
    }
}
