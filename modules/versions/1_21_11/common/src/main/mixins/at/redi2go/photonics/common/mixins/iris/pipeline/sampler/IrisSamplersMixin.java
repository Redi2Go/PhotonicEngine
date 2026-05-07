package at.redi2go.photonics.common.mixins.iris.pipeline.sampler;

import at.redi2go.photonics.common.iris.IrisUtil;
import at.redi2go.photonics.core.iris.pipeline.texture.ISamplerHolder;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.gl.texture.TextureAccess;
import net.irisshaders.iris.samplers.IrisSamplers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IrisSamplers.class)
public abstract class IrisSamplersMixin {
    @Inject(
            method = "addCustomTextures",
            at = @At("TAIL")
    )
    private static void addCustomTextures(
            SamplerHolder samplers,
            Object2ObjectMap<String, TextureAccess> irisCustomTextures,
            CallbackInfo ci
    ) {
        IrisUtil.getPhotonics()
                .ifPresent((e) -> e.registerCustomTextures((ISamplerHolder) samplers));
    }
}
