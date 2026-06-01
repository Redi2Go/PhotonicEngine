package at.redi2go.photonics.core.rendering.world.registry.block.builder;

import at.redi2go.photonics.core.rendering.world.bakery.VoxelConsumer;
import at.redi2go.photonics.core.rendering.world.block.TextureData;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockRegistry;
import at.redi2go.photonics.core.rendering.world.registry.block.model.BlockModelImpl;
import at.redi2go.photonics.core.rendering.world.registry.block.model.BlockModelRegistry;
import at.redi2go.photonics.core.rendering.world.registry.block.model.BlockPartImpl;
import at.redi2go.photonics.core.rendering.world.registry.object.WeakValue;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.Map;

public class BlockModelBuilder implements VoxelConsumer {
    private final VoxelData tempVoxelData = new VoxelData();
    private final Vector3i tempPos = new Vector3i();

    private final Map<Vector3i, BlockLayerBuilder> parts = new HashMap<>();

    private int lastPartHash = 0;
    private BlockLayerBuilder lastPart = null;

    @Override
    public void acceptVoxel(int x, int y, int z, int normal, TextureData textureData) {
        BlockLayerBuilder builder;

        Vector3i blockPos = new Vector3i(Math.floorDiv(x, 16), Math.floorDiv(y, 16), Math.floorDiv(z, 16));
        int hash = blockPos.hashCode();

        if (lastPartHash != hash || lastPart == null) {
            builder = parts.computeIfAbsent(blockPos, (ignored) -> new BlockLayerBuilder());

            lastPartHash = hash;
            lastPart = builder;
        } else builder = lastPart;

        tempPos.x = correctVoxelPos(x);
        tempPos.y = correctVoxelPos(y);
        tempPos.z = correctVoxelPos(z);

        tempVoxelData.normal = normal;
        tempVoxelData.textureData = textureData;

        builder.insertEntry(tempPos, tempVoxelData);
    }

    public @WeakValue @Nullable BlockModelImpl build(long vertexHash, BlockRegistry blockRegistry, BlockModelRegistry registry) {
        if (this.parts.isEmpty()) return null;

        ImmutableList.Builder<BlockPartImpl> parts = ImmutableList.builder();

        for (var entry : this.parts.entrySet()) {
            var offset = entry.getKey();
            var buildPart = entry.getValue().build(blockRegistry);

            parts.add(new BlockPartImpl(offset, buildPart));
        }

        return new BlockModelImpl(vertexHash, parts.build(), registry);
    }

    private static int correctVoxelPos(int component) {
        if (component < 0)
            return 15 - ((-component - 1) & 15);

        return component & 15;
    }
}
