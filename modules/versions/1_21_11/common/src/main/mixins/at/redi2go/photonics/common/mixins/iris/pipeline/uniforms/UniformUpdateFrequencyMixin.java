package at.redi2go.photonics.common.mixins.iris.pipeline.uniforms;

import at.redi2go.photonics.core.iris.pipeline.uniform.IUniformUpdateFrequency;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(UniformUpdateFrequency.class)
public abstract class UniformUpdateFrequencyMixin implements IUniformUpdateFrequency {
}
