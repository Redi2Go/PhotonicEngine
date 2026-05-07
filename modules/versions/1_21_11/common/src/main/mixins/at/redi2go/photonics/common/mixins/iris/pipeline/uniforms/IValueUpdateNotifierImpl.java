package at.redi2go.photonics.common.mixins.iris.pipeline.uniforms;

import at.redi2go.photonics.core.iris.pipeline.uniform.IValueUpdateNotifier;
import net.irisshaders.iris.gl.state.ValueUpdateNotifier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IValueUpdateNotifier.class)
public interface IValueUpdateNotifierImpl extends ValueUpdateNotifier {
}
