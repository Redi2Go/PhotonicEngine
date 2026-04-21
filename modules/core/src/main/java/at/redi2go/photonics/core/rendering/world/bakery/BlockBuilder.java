package at.redi2go.photonics.core.rendering.world.bakery;

import at.redi2go.photonics.api.mc.Id;
import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.core.rendering.world.bakery.texture.CpuTexture;
import at.redi2go.photonics.core.rendering.world.block.VoxelColor;
import org.joml.Vector3d;
import org.joml.Vector3f;

// TODO: Check arguments are in the correct order
public interface BlockBuilder {
    BlockBuilder useAtlas(Id id);

    BlockBuilder useTexture(CpuTexture texture);

    BlockBuilder useOffset(float x, float y, float z);

    default BlockBuilder useOffset(Vector3f offset) {
        return useOffset(offset.x, offset.y, offset.z);
    }

    BlockBuilder beginBlock(int blockId, Vector3d blockVoxelPos);

    BlockBuilder addVertex(float x, float y, float z);

    BlockBuilder setTint(int argb);

    default BlockBuilder setTint(int r, int g, int b, int a) {
        return setTint(VoxelColor.from(r, g, b, a));
    }

    BlockBuilder setUv(float u, float v);
}
