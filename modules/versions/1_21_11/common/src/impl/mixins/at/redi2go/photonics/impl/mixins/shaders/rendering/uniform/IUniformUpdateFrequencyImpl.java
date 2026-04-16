package at.redi2go.photonics.impl.mixins.shaders.rendering.uniform;

import at.redi2go.photonics.api.shaders.rendering.uniform.IUniformUpdateFrequency;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(IUniformUpdateFrequency.class)
public interface IUniformUpdateFrequencyImpl {
    @Overwrite
    static IUniformUpdateFrequency once() {
        return (IUniformUpdateFrequency) (Object) UniformUpdateFrequency.ONCE;
    }

    @Overwrite
    static IUniformUpdateFrequency perTick() {
        return (IUniformUpdateFrequency) (Object) UniformUpdateFrequency.PER_TICK;
    }

    @Overwrite
    static IUniformUpdateFrequency perFrame() {
        return (IUniformUpdateFrequency) (Object) UniformUpdateFrequency.PER_FRAME;
    }

    @Overwrite
    static IUniformUpdateFrequency custom() {
        return (IUniformUpdateFrequency) (Object) UniformUpdateFrequency.CUSTOM;
    }
}
