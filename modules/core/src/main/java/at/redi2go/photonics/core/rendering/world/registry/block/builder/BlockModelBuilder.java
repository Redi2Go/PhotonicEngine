package at.redi2go.photonics.core.rendering.world.registry.block.builder;

import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.rendering.world.bakery.VoxelConsumer;
import at.redi2go.photonics.core.rendering.world.block.TextureData;
import at.redi2go.photonics.core.rendering.world.block.palette.TintBuilder;
import at.redi2go.photonics.core.rendering.world.registry.MemoryOwner;
import at.redi2go.photonics.core.rendering.world.registry.PaletteObject;
import at.redi2go.photonics.core.rendering.world.registry.WorldRegistry;
import at.redi2go.photonics.core.rendering.world.registry.block.template.BlockModelTemplate;
import at.redi2go.photonics.core.rendering.world.registry.block.template.BlockPartTemplate;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.joml.Vector3i;

import java.util.List;
import java.util.Map;

public class BlockModelBuilder implements VoxelConsumer {
    private final WorldRegistry worldRegistry;
    private final long vertexHash;
    private final TintBuilder.Result tint;

    private final Int2IntMap tintPaletteIndexes;
    private final Map<Vector3i, BlockPartBuilder> parts;

    private int lastPartHash = 0;
    private BlockPartBuilder lastPart = null;

    public BlockModelBuilder(
            WorldRegistry worldRegistry,
            long vertexHash,
            TintBuilder.Result tint
    ) {
        this.worldRegistry = worldRegistry;
        this.vertexHash = vertexHash;
        this.tint = tint;
        this.tintPaletteIndexes = new Int2IntOpenHashMap();
        this.parts = new Object2ObjectOpenHashMap<>();
    }

    @Override
    public void acceptVoxel(
            int x, int y, int z,
            int normal,
            int tint,
            TextureData textureData
    ) {
        BlockPartBuilder builder;

        Vector3i blockPos =  new Vector3i(x, y, z).div(16);
        int hash = blockPos.hashCode();
        if (lastPartHash != hash || lastPart == null) {
            builder = parts.computeIfAbsent(
                    blockPos,
                    (ignored) -> new BlockPartBuilder()
            );

            lastPartHash = hash;
            lastPart = builder;
        } else builder = lastPart;

        tintPaletteIndexes.putIfAbsent(tint, tintPaletteIndexes.size());
        builder.acceptVoxel(
                correctVoxelPos(x),
                correctVoxelPos(y),
                correctVoxelPos(z),
                normal,
                tint,
                textureData
        );
    }

    public BlockModelTemplate build() {
        ImmutableList.Builder<BlockPartTemplate> partTemplates = ImmutableList.builder();

        Int2IntMap tintIndexes = tint.indexes();
        for (var entry : parts.entrySet()) {
            var offset = entry.getKey();
            var builtPart = entry.getValue().build();

            var palette = builtPart.palette();

            int[] tintMappings = new int[palette.size()];
            MemoryOwner.ManagedRef<PaletteObject.Entry>[] paletteArray = new MemoryOwner.ManagedRef[palette.size()];

            for (int i = 0; i < paletteArray.length; i++) {
                paletteArray[i] = worldRegistry.allocatePalette(palette.get(i));

                int tint = palette.getTint(i);
                tintMappings[i] = tintIndexes.get(tint);
            }

            var blockVoxel = worldRegistry.allocateBlockVoxel(
                    builtPart.hash(),
                    builtPart.voxelData()
            );

            partTemplates.add(
                new BlockPartTemplate(
                        worldRegistry,
                        offset,
                        builtPart.volume(),
                        tintMappings,
                        List.of(paletteArray),
                        blockVoxel
                )
            );
        }

        return new BlockModelTemplate(
                worldRegistry,
                vertexHash,
                partTemplates.build()
        );
    }

    private static int correctVoxelPos(int component) {
        return component < 0 ? 15 - (-component & 15) : component & 15;
    }
}
