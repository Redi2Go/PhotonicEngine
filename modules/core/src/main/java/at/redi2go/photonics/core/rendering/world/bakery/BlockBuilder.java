package at.redi2go.photonics.core.rendering.world.bakery;

import at.redi2go.photonics.api.mc.Id;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasTexture;
import at.redi2go.photonics.core.rendering.world.block.VoxelColor;
import org.joml.Vector3f;

public interface BlockBuilder {
    BlockBuilder useAtlas(Id id);

    BlockBuilder useTexture(AtlasTexture texture);

    BlockBuilder useOffset(float x, float y, float z);

    BlockBuilder useBlockId(int blockId);

    default BlockBuilder useOffset(Vector3f offset) {
        return useOffset(offset.x, offset.y, offset.z);
    }

    BlockBuilder addVertex(float x, float y, float z);

    BlockBuilder setTint(int argb);

    default BlockBuilder setTint(int r, int g, int b, int a) {
        return setTint(VoxelColor.from(r, g, b, a));
    }

    BlockBuilder setUv(float u, float v);
}
