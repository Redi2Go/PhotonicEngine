package at.redi2go.photonics.common.iris.pipeline.uniforms;

import at.redi2go.photonics.engine.iris.IrisManager;
import at.redi2go.photonics.engine.iris.pipeline.uniforms.IrisDynamicUniformHolder;
import at.redi2go.photonics.engine.iris.pipeline.uniforms.IrisUniformHolder;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.uniform.DynamicUniformHolder;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.shaderpack.IdMap;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommonUniforms.class)
public abstract class CommonUniformsMixin {
    @Inject(
            method = "addNonDynamicUniforms",
            at = @At("TAIL")
    )
    private static void addNonDynamicUniforms(UniformHolder uniforms, IdMap idMap, PackDirectives directives, FrameUpdateNotifier updateNotifier, CallbackInfo ci) {
        IrisManager.registerUniforms((IrisUniformHolder) uniforms);
    }

    @Inject(
            method = "addDynamicUniforms",
            at = @At("TAIL")
    )
    private static void addDynamicUniforms(DynamicUniformHolder uniforms, FogMode fogMode, CallbackInfo ci) {
        IrisManager.registerDynamicUniforms((IrisDynamicUniformHolder) uniforms);
    }
}
