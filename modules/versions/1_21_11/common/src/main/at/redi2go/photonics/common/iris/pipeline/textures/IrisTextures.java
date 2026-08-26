package at.redi2go.photonics.common.iris.pipeline.textures;

import at.redi2go.photonics.game.blaze3d.textures.IGpuSampler;
import at.redi2go.photonics.game.blaze3d.textures.IGpuTexture;
import at.redi2go.photonics.impl.blaze3d.opengl.textures.GlTexture;
import at.redi2go.photonics.impl.blaze3d.opengl.textures.GlTexture1D;
import at.redi2go.photonics.impl.blaze3d.opengl.textures.GlTexture2D;
import at.redi2go.photonics.impl.blaze3d.opengl.textures.GlTexture3D;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.texture.TextureType;

public class IrisTextures {
    public static TextureType getTextureType(IGpuTexture texture) {
        return switch (texture) {
            case GlTexture1D ignored -> TextureType.TEXTURE_1D;
            case GlTexture2D ignored -> TextureType.TEXTURE_2D;
            case GlTexture3D ignored -> TextureType.TEXTURE_3D;

            default -> throw new IllegalArgumentException("Unknown texture type " + texture.getClass().getSimpleName());
        };
    }

    public static int getTextureHandle(IGpuTexture texture) {
        return ((GlTexture) texture).ph$getHandle();
    }

    public static GlSampler getGlSampler(IGpuSampler sampler) {
        return new GlSampler(((GlSamplerAccessor) sampler).getId());
    }

    private IrisTextures() { }
}
