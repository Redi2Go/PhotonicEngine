package at.redi2go.photonics.common.iris.pipeline.uniforms;

import at.redi2go.photonics.engine.iris.pipeline.uniforms.IValueUpdateNotifier;
import net.irisshaders.iris.gl.state.ValueUpdateNotifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(IValueUpdateNotifier.class)
public interface ValueUpdateNotifierImpl extends ValueUpdateNotifier {

}
