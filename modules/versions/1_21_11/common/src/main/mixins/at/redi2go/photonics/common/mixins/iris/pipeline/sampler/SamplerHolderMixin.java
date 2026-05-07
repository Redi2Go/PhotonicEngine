package at.redi2go.photonics.common.mixins.iris.pipeline.sampler;

import at.redi2go.photonics.api.gpu.textures.IGpuTexture;
import at.redi2go.photonics.common.iris.IrisUtil;
import at.redi2go.photonics.core.iris.pipeline.texture.ISamplerHolder;
import at.redi2go.photonics.impl.mc.blaze3d.opengl.textures.AbstractGlTexture;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Supplier;

@Mixin(SamplerHolder.class)
public interface SamplerHolderMixin extends SamplerHolder, ISamplerHolder {
    @Override
    default void addSampler(
            String name,
            Supplier<IGpuTexture.WithSampler<?>> textureAndSampler
    ) {
        addDynamicSampler(
                IrisUtil.getTextureType(textureAndSampler.get().texture()),
                () -> IrisUtil.getTextureHandle(textureAndSampler.get().texture()),
                () -> IrisUtil.getGlSampler(textureAndSampler.get().sampler()),
                name
        );
    }

    @Override
    default void addDefaultSampler(String name, Supplier<IGpuTexture<?>> texture) {
        addDynamicSampler(
                IrisUtil.getTextureType(texture.get()),
                () -> IrisUtil.getTextureHandle(texture.get()),
                null,
                name
        );
    }
}
