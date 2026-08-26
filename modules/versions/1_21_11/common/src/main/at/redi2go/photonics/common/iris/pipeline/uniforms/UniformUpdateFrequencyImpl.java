package at.redi2go.photonics.common.iris.pipeline.uniforms;

import at.redi2go.photonics.engine.iris.pipeline.uniforms.IUniformUpdateFrequency;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(UniformUpdateFrequency.class)
public abstract class UniformUpdateFrequencyImpl implements IUniformUpdateFrequency {
    @Mixin(IUniformUpdateFrequency.class)
    public interface StaticMethods {
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
}
