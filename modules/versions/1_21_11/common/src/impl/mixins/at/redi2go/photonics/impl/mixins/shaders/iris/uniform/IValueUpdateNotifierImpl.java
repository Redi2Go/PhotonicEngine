package at.redi2go.photonics.impl.mixins.shaders.iris.uniform;

import at.redi2go.photonics.api.shaders.uniform.IValueUpdateNotifier;
import net.irisshaders.iris.gl.state.ValueUpdateNotifier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IValueUpdateNotifier.class)
public interface IValueUpdateNotifierImpl extends ValueUpdateNotifier {
}
