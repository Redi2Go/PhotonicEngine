package at.redi2go.photonics.core.rendering.world.bakery.texture;

import at.redi2go.photonics.core.rendering.world.block.TextureData;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class AtlasTexture {
    private final CpuTexture albedo;
    private final @Nullable CpuTexture normal;
    private final @Nullable CpuTexture specular;

    public AtlasTexture(CpuTexture albedo, @Nullable CpuTexture normal, @Nullable CpuTexture specular) {
        Objects.requireNonNull(albedo, "albedo was null");

        this.albedo = albedo;
        this.normal = normal;
        this.specular = specular;
    }

    public TextureData sample(
            int blockId,
            float u,
            float v
    ) {
        return new TextureData(
                blockId,
                albedo.sample(u, v),
                normal != null ? normal.sample(u, v) : TextureData.DEFAULT_NORMAL,
                specular != null ? specular.sample(u, v) : TextureData.DEFAULT_SPECULAR
        );
    }
}
