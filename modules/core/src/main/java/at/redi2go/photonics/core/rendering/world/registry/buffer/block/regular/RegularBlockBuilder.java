package at.redi2go.photonics.core.rendering.world.registry.buffer.block.regular;

import at.redi2go.photonics.core.model.VoxelEntry;
import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.RegionMapping;
import at.redi2go.photonics.core.rendering.world.RtVoxel;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.MutablePaletteEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import at.redi2go.photonics.core.rendering.world.registry.buffer.BufferBlockRegistry;
import at.redi2go.photonics.core.rendering.world.registry.buffer.BufferObject;
import at.redi2go.photonics.core.rendering.world.registry.buffer.BufferPaletteObject;
import at.redi2go.photonics.core.rendering.world.registry.buffer.block.AbstractBlockBuilder;
import at.redi2go.photonics.core.rendering.world.registry.buffer.block.BufferBlockEntry;
import at.redi2go.photonics.core.rendering.world.registry.buffer.block.BufferBlockHeader;
import at.redi2go.photonics.core.rendering.world.registry.buffer.block.BufferBlockVoxel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RegularBlockBuilder extends AbstractBlockBuilder {
    public RegularBlockBuilder(BufferBlockRegistry registry, RegionMapping mapping) {
        super(registry, mapping);
    }

    public RegularBlockBuilder(BufferBlockRegistry registry) {
        super(registry);
    }

    private @Nullable MutablePaletteEntry insertEntry(int voxelIndex, short region) {
        MutablePaletteEntry entry = data[voxelIndex];

        if (entry != null) {
            if (getRegion(voxelIndex) != region) return null;
        } else {
            entry = new MutablePaletteEntry();
            data[voxelIndex] = entry;

            setRegion(voxelIndex, region);
        }

        isEmpty = false;

        return entry;
    }

    @Override
    public void insertVoxel(int x, int y, int z, short region, int normal, int tint, TextureData textureData) {
        int voxelIndex = VoxelModel.toVoxelIndex(x & 15, y & 15, z & 15);

        var entry = insertEntry(voxelIndex, region);
        if (entry == null) return;

        entry.update(normal, tint, textureData);
    }

    @Override
    public void insertBlock(int x, int y, int z, short region, BlockEntry block) {
        var header = ((BufferBlockEntry) block).header();
        if (!header.isAllocated()) throw new IllegalStateException();

        BufferBlockVoxel voxel = header.blockVoxel();
        var buffer = voxel.buffer();

        for (int voxelIndex = 0; voxelIndex < RtVoxel.ENTRIES_SIZE; voxelIndex++) {
            int voxelEntry = buffer.get(voxelIndex);
            if (VoxelEntry.isAir(voxelEntry)) continue;

            voxelEntry = VoxelEntry.getData(voxelEntry) >> 1;

            var paletteEntry = header.getPaletteEntry(voxelEntry);
            var tint = header.getTint(voxelEntry);

            var entry = insertEntry(voxelIndex, region);
            if (entry == null) continue;

            entry.update(tint, paletteEntry);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable BlockEntry build() {
        if (isEmpty) return null;

        var palette = buildPalette();
        var regionBuilder = new RegionBuilder();

        var result = buildBlockVoxel(palette, regionBuilder);
        var blockVoxel = registry.allocateBlockVoxel(result.hash(), result.voxelData());

        BufferObject.ManagedRef<BufferPaletteObject.Entry>[] paletteArray = new BufferObject.ManagedRef[palette.size()];
        int[] tint = new int[palette.size()];

        for (int i = 0; i < palette.size(); i++) {
            paletteArray[i] = registry.allocatePalette(palette.get(i));
            tint[i] = palette.getTint(i);
        }

        var paletteList = List.of(paletteArray);

        return new RegularBlockEntry(
                regionBuilder,
                registry.allocateBlockHeader(
                        blockVoxel,
                        skylight,
                        paletteList,
                        tint,
                        BufferBlockHeader.voxelHash(paletteList, blockVoxel.get()),
                        BufferBlockHeader.tintHash(tint)
                ).elevate()
        );
    }

    @Override
    public void close() {

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
