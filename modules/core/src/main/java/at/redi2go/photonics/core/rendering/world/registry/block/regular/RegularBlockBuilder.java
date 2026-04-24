package at.redi2go.photonics.core.rendering.world.registry.block.regular;

import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.RegionMapping;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.MutablePaletteEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import at.redi2go.photonics.core.rendering.world.registry.BufferBlockRegistry;
import at.redi2go.photonics.core.rendering.world.registry.PaletteAllocation;
import at.redi2go.photonics.core.rendering.world.registry.block.AbstractBlockBuilder;
import at.redi2go.photonics.core.rendering.world.registry.block.BufferBlockHeader;
import org.jetbrains.annotations.Nullable;

public class RegularBlockBuilder extends AbstractBlockBuilder {
    public RegularBlockBuilder(BufferBlockRegistry registry, RegionMapping mapping) {
        super(registry, mapping);
    }

    public RegularBlockBuilder(BufferBlockRegistry registry) {
        super(registry);
    }

    @Override
    public void insertVoxel(int x, int y, int z, short region, int normal, int tint, TextureData textureData) {
        int voxelIndex = VoxelModel.toVoxelIndex(x & 15, y & 15, z & 15);
        MutablePaletteEntry entry = data[voxelIndex];

        if (entry != null) {
            if (getRegion(voxelIndex) != region) return;
        } else {
            entry = new MutablePaletteEntry();
            data[voxelIndex] = entry;

            setRegion(voxelIndex, region);
        }

        isEmpty = false;

        entry.update(normal, tint, textureData);
    }

    @Override
    public @Nullable BlockEntry build() {
        if (isEmpty) return null;

        var palette = buildPalette();
        var regionBuilder = new RegionBuilder();

        var result = buildBlockVoxel(palette, regionBuilder);
        var blockVoxel = registry.allocateBlockVoxel(result.hash(), result.voxelData());

        PaletteAllocation[] paletteArray = new PaletteAllocation[palette.size()];
        int[] tint = new int[palette.size()];

        for (int i = 0; i < palette.size(); i++) {
            paletteArray[i] = registry.allocatePalette(palette.get(i));
            tint[i] = palette.getTint(i);
        }

        return new RegularBlockEntry(
                regionBuilder,
                registry.allocateBlockHeader(
                        blockVoxel,
                        skylight,
                        paletteArray,
                        tint,
                        BufferBlockHeader.voxelHash(paletteArray, blockVoxel),
                        BufferBlockHeader.tintHash(tint)
                )
        );
    }

    private class RegionBuilder extends RegionMapping implements AbstractBlockBuilder.RegionBuilder {
        public void set(int voxelIndex) {
            setRegion(
                    voxelIndex,
                    RegularBlockBuilder.this.getRegion(voxelIndex)
            );
        }
    }
}
