package at.redi2go.photonics.core.rendering.world.bakery;

import at.redi2go.photonics.api.mc.Id;
import at.redi2go.photonics.core.rendering.world.bakery.texture.CpuTexture;
import at.redi2go.photonics.core.rendering.world.block.VoxelColor;

// TODO: Check arguments are in the correct order
public interface VertexBuilder {
    VertexBuilder useAtlas(Id id);

    VertexBuilder useTexture(CpuTexture texture);

    VertexBuilder useBlockId(int blockId);

    VertexBuilder addVertex(float x, float y, float z);

    VertexBuilder setTint(int argb);

    default VertexBuilder setTint(int r, int g, int b, int a) {
        return setTint(VoxelColor.from(r, g, b, a));
    }

    VertexBuilder setUv(float u, float v);
}
