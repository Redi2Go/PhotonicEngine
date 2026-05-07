package at.redi2go.photonics.common.mixins.iris.pipeline.sampler;

import com.mojang.blaze3d.opengl.GlSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlSampler.class)
public interface GlSamplerAccessor {
    @Accessor
    int getId();
}
