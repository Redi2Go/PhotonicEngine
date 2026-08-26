package at.redi2go.photonics.common.iris.pipeline.textures;

import at.redi2go.photonics.engine.iris.pipeline.textures.IrisSamplerHolder;
import at.redi2go.photonics.game.blaze3d.textures.IGpuTexture;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Supplier;

@Mixin(SamplerHolder.class)
public interface IrisSamplerHolderImpl extends SamplerHolder, IrisSamplerHolder {
    @Override
    default void addSampler(
            String name,
            Supplier<IGpuTexture.WithSampler> textureAndSampler
    ) {
        addDynamicSampler(
                IrisTextures.getTextureType(textureAndSampler.get().texture()),
                () -> IrisTextures.getTextureHandle(textureAndSampler.get().texture()),
                () -> IrisTextures.getGlSampler(textureAndSampler.get().sampler()),
                name
        );
    }

    @Override
    default void addDefaultSampler(String name, Supplier<IGpuTexture> texture) {
        addDynamicSampler(
                IrisTextures.getTextureType(texture.get()),
                () -> IrisTextures.getTextureHandle(texture.get()),
                null,
                name
        );
    }
}
