package at.redi2go.photonics.core.rendering.world.registry.block.regular;

import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.RegionMapping;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.MutablePaletteEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import at.redi2go.photonics.core.rendering.world.registry.BufferBlockRegistry;
import at.redi2go.photonics.core.rendering.world.registry.PaletteAllocation;
import at.redi2go.photonics.core.rendering.world.registry.block.AbstractBlockBuilder;
import at.redi2go.photonics.core.rendering.world.registry.block.AbstractBlockVoxel;
import at.redi2go.photonics.core.util.IntPacking;
import org.jetbrains.annotations.Nullable;

public class RegularBlockBuilder extends AbstractBlockBuilder {
    private boolean isEmpty = true;

    public RegularBlockBuilder(BufferBlockRegistry registry, RegionMapping mapping) {
        super(registry, mapping);
    }

    public RegularBlockBuilder(BufferBlockRegistry registry) {
        super(registry);
    }

    @Override
    public boolean insert(int x, int y, int z, short region, int normal, int tint, TextureData textureData) {
        int voxelIndex = VoxelModel.toVoxelIndex(x & 15, y & 15, z & 15);
        MutablePaletteEntry entry = data[voxelIndex];

        if (entry != null) {
            if (getRegion(voxelIndex) != region)
                return false;
        } else {
            entry = new MutablePaletteEntry();
            data[voxelIndex] = entry;

            setRegion(voxelIndex, region);
        }

        isEmpty = false;

        return entry.update(normal, tint, textureData);
    }

    @Override
    public @Nullable BlockEntry build() {
        if (isEmpty) return null;

        var palette = buildPalette();
        var regionBuilder = new RegionBuilder();

        var result = buildBlockVoxel(palette, regionBuilder);
        var blockVoxel = registry.allocateRegularBlockVoxel(result.hash(), result.shift(), result.voxelData());

        PaletteAllocation[] paletteArray = new PaletteAllocation[palette.size()];
        int[] tint = new int[palette.size()];

        for (int i = 0; i < palette.size(); i++) {
            paletteArray[i] = registry.allocatePalette(palette.get(i));
            tint[i] = palette.getTint(i);
        }

        var variant = registry.allocateRegularBlockVariant(
                blockVoxel,
                result.shift(),
                IntPacking.valueMask(result.shift()),
                skylight,
                paletteArray,
                tint
        );

        variant.acquire();

        return new RegularBlockEntry(regionBuilder, variant);
    }

    private class RegionBuilder extends RegionMapping  implements AbstractBlockBuilder.RegionBuilder {
        public void set(int voxelIndex) {
            setRegion(
                    voxelIndex,
                    RegularBlockBuilder.this.getRegion(voxelIndex)
            );
        }
    }
}
