package at.redi2go.photonics.core.rendering.world.registry.buffer.block.variant;

import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.BlockVariantBuilder;
import at.redi2go.photonics.core.rendering.world.block.palette.MutablePaletteEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import at.redi2go.photonics.core.rendering.world.block.palette.TintBuilder;
import at.redi2go.photonics.core.rendering.world.registry.buffer.BufferBlockRegistry;
import at.redi2go.photonics.core.rendering.world.registry.buffer.BufferObject;
import at.redi2go.photonics.core.rendering.world.registry.buffer.BufferPaletteObject;
import at.redi2go.photonics.core.rendering.world.registry.buffer.block.AbstractBlockBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BufferBlockVariantBuilder extends AbstractBlockBuilder implements BlockVariantBuilder {
    private final long vertexHash;
    private final BufferBlockVariantFuture future;

    private final Int2IntMap tintIndexes = new Int2IntOpenHashMap();

    public BufferBlockVariantBuilder(
            BufferBlockRegistry registry,
            long vertexHash,
            BufferBlockVariantFuture future
    ) {
        super(registry);

        this.vertexHash = vertexHash;
        this.future = future;
    }

    @Override
    public void setRegion(short region) {
        initRegion(region);
    }

    @Override
    public void propagateException(Throwable ex) {
        future.completeExceptionally(ex);
    }

    @Override
    public void insertVoxel(int x, int y, int z, short region, int normal, int tint, TextureData textureData) {
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
    @SuppressWarnings("unchecked")
    public @Nullable BlockEntry build() {
        if (isEmpty) {
            future.complete(null);
            return null;
        }

        var palette = buildPalette();
        var result = buildBlockVoxel(palette, EmptyRegionBuilder.INSTANCE);

        BufferObject.ManagedRef<BufferPaletteObject.Entry>[] paletteArray = new BufferObject.ManagedRef[palette.size()];
        int[] tintMappings = new int[palette.size()];
        TintBuilder tintBuilder = new TintBuilder();

        for (int i = 0; i < palette.size(); i++) {
            paletteArray[i] = registry.allocatePalette(palette.get(i));
            int tint = palette.getTint(i);

            tintBuilder.add(tint);
            tintMappings[i] = tintIndexes.get(tint);
        }

        var blockVoxel = registry.allocateBlockVoxel(
                result.hash(),
                result.voxelData()
        );

        var variant = blockVoxel.get().newVariant(
                vertexHash,
                List.of(paletteArray),
                tintMappings
        );

        future.complete(variant);

        return variant.get().createVariant(tintBuilder.build(), skylight, singleGetRegion());
    }

    @Override
    public @Nullable BlockEntry createVariant(TintBuilder.Result tintInfo, int skylight, short region) {
        throw new UnsupportedOperationException("createVariant");
    }

    @Override
    public void close() {

    }


    private static class EmptyRegionBuilder implements RegionBuilder {
        static final EmptyRegionBuilder INSTANCE = new EmptyRegionBuilder();

        @Override
        public void set(int voxelIndex) {

        }
    }
}
