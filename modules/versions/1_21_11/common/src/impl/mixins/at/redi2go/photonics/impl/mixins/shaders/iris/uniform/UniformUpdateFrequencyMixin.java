package at.redi2go.photonics.impl.mixins.shaders.iris.uniform;

import at.redi2go.photonics.api.shaders.uniform.IUniformUpdateFrequency;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(UniformUpdateFrequency.class)
public abstract class UniformUpdateFrequencyMixin implements IUniformUpdateFrequency {
}
