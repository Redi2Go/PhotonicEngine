package at.redi2go.photonics.common.mixins.iris.pipeline.uniforms;

import at.redi2go.photonics.api.shaders.uniform.IDynamicUniformHolder;
import at.redi2go.photonics.api.shaders.uniform.IUniformHolder;
import at.redi2go.photonics.common.iris.IrisUtil;
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
public class CommonUniformsMixin {
    @Inject(
            method = "addNonDynamicUniforms",
            at = @At("TAIL")
    )
    private static void addNonDynamicUniforms(UniformHolder uniforms, IdMap idMap, PackDirectives directives, FrameUpdateNotifier updateNotifier, CallbackInfo ci) {
        IrisUtil.getPhotonics()
                .ifPresent(e -> e.registerUniforms((IUniformHolder) uniforms));
    }

    @Inject(
            method = "addDynamicUniforms",
            at = @At("TAIL")
    )
    private static void addDynamicUniforms(DynamicUniformHolder uniforms, FogMode fogMode, CallbackInfo ci) {
        IrisUtil.getPhotonics()
                .ifPresent(e -> e.registerDynamicUniforms((IDynamicUniformHolder) uniforms));
    }
}
