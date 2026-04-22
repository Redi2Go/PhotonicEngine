package at.redi2go.photonics.core.rendering.world.registry.block.contained;

import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.block.ContainedBlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.MutablePaletteEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import at.redi2go.photonics.core.rendering.world.registry.BufferBlockRegistry;
import at.redi2go.photonics.core.rendering.world.registry.PaletteAllocation;
import at.redi2go.photonics.core.rendering.world.registry.block.AbstractBlockBuilder;
import at.redi2go.photonics.core.util.IntPacking;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import org.jetbrains.annotations.Nullable;

public class ContainedBlockBuilder extends AbstractBlockBuilder implements ContainedBlockEntry.Builder, ContainedBlockEntry.Factory {
    private final long vertexHash;
    private final ContainedBlockFuture future;

    private final Int2IntMap tintIndexes = new Int2IntOpenHashMap();

    private boolean isEmpty = true;

    public ContainedBlockBuilder(BufferBlockRegistry registry, long vertexHash, ContainedBlockFuture future) {
        super(registry);

        this.vertexHash = vertexHash;
        this.future = future;
    }

    @Override
    public void propagateException(Throwable ex) {
        future.completeExceptionally(ex);
    }

    @Override
    public void setRegion(short region) {
        initRegion(region);
    }

    @Override
    public void insertVoxel(
            int x, int y, int z,
            short region,
            int normal,
            int tint,
            TextureData textureData
    ) {
        if (!VoxelModel.contains(x, y, z, 16, 16, 16)) return;

        isEmpty = false;

        int voxelIndex = VoxelModel.toVoxelIndex(x & 15, y & 15, z & 15);
        MutablePaletteEntry entry = data[voxelIndex];

        if (entry == null) {
            entry = new MutablePaletteEntry();
            data[voxelIndex] = entry;
        }

        tintIndexes.putIfAbsent(tint, tintIndexes.size());
        entry.update(normal, tint, textureData);
    }

    @Override
    public void acceptVoxel(int x, int y, int z, short region, int normal, int tint, TextureData textureData) {
        insertVoxel(x, y, z, region, normal, tint, textureData);
    }

    @Override
    public @Nullable ContainedBlockEntry build() {
        if (isEmpty) {
            future.complete(null);
        }

        var palette = buildPalette();
        var result = buildBlockVoxel(palette, EmptyRegionBuilder.INSTANCE);

        PaletteAllocation[] paletteArray = new PaletteAllocation[palette.size()];
        int[] tintMappings = new int[palette.size()];
        IntArraySet tints = new IntArraySet();

        for (int i = 0; i < palette.size(); i++) {
            paletteArray[i] = registry.allocatePalette(palette.get(i));
            int tint = palette.getTint(i);

            tints.add(tint);
            tintMappings[i] = tintIndexes.get(tint);
        }


        var blockVoxel = registry.allocateContainedBlockVoxel(
                result.shift(),
                IntPacking.valueMask(result.shift()),
                result.hash(),
                vertexHash,
                result.voxelData(),
                tintMappings,
                paletteArray
        );

        future.complete(blockVoxel);

        return blockVoxel.createVariant(tints, skylight, singleGetRegion());
    }

    @Override
    public ContainedBlockEntry createVariant(IntArraySet tint, int skylight, short region) {
        throw new UnsupportedOperationException("createVariant");
    }

    private static class EmptyRegionBuilder implements RegionBuilder {
        static final EmptyRegionBuilder INSTANCE = new EmptyRegionBuilder();

        @Override
        public void set(int voxelIndex) {

        }
    }
}
