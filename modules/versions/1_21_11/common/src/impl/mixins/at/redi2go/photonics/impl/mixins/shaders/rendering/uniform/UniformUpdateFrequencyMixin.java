package at.redi2go.photonics.impl.mixins.shaders.rendering.uniform;

import at.redi2go.photonics.api.shaders.rendering.uniform.IUniformUpdateFrequency;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(UniformUpdateFrequency.class)
public class UniformUpdateFrequencyMixin implements IUniformUpdateFrequency {
}
