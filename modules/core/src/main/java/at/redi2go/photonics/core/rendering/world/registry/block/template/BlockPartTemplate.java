package at.redi2go.photonics.core.rendering.world.registry.block.template;

import at.redi2go.photonics.core.rendering.world.block.palette.TintBuilder;
import at.redi2go.photonics.core.rendering.world.registry.ManagedObject;
import at.redi2go.photonics.core.rendering.world.registry.PaletteObject;
import at.redi2go.photonics.core.rendering.world.registry.WorldRegistry;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockHeader;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockVoxel;
import org.joml.Vector3i;

import java.util.List;

public class BlockPartTemplate extends ManagedObject<BlockPartTemplate> {
    private final Vector3i offset;
    private final int boundingVolume;

    private final int[] tintMapping;

    private ManagedRef<BlockModelTemplate> modelTemplate;

    private final List<ManagedRef<PaletteObject.Entry>> palette;
    private final ManagedRef<BlockVoxel> blockVoxel;

    private final long voxelHash;

    public BlockPartTemplate(
            WorldRegistry registry,
            Vector3i offset,
            int boundingVolume,
            int[] tintMapping,
            List<ManagedRef<PaletteObject.Entry>> palette,
            ManagedRef<BlockVoxel> blockVoxel
    ) {
        super(registry);

        this.offset = offset;
        this.boundingVolume = boundingVolume;

        this.tintMapping = tintMapping;
        this.palette = palette;
        this.blockVoxel = blockVoxel;

        this.voxelHash = BlockHeader.voxelHash(palette, blockVoxel.get());
    }

    @Override
    protected void loadDependants(List<ManagedRef<?>> output) {
        output.add(modelTemplate);

        output.addAll(palette);
        output.add(blockVoxel);
    }

    public void setModelTemplate(BlockModelTemplate template) {
        this.modelTemplate = template.makeManagedRef();
        acquireDependants();
    }

    @Override
    protected BlockPartTemplate getWrappedValue() {
        return this;
    }

    public Vector3i offset() {
        return offset;
    }

    public int boundingVolume() {
        return boundingVolume;
    }

    public ManagedRef<BlockHeader> createVariant(TintBuilder.Result tint) {
        int[] tintValues = new int[palette.size()];
        int[] blockTint = tint.tints().toIntArray();

        for (int i = 0; i < tintValues.length; i++)
            tintValues[i] = blockTint[tintMapping[i]];

        return registry.allocateBlockHeader(
                tintValues,
                palette,
                blockVoxel,
                voxelHash,
                BlockHeader.tintHash(tintValues)
        );
    }
}
