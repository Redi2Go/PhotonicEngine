package at.redi2go.photonics.common.mixins.iris.pipeline.passes;

import at.redi2go.photonics.common.iris.IrisUtil;
import net.caffeinemc.mods.sodium.client.gl.shader.GlProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlProgram.class)
public abstract class GlProgramMixin {
    @Inject(
            method = "bind",
            at = @At("TAIL"),
            remap = false
    )
    public void applyTail(CallbackInfo ci) {
        IrisUtil.bindBuffers(((GlProgram<?>) (Object) this).handle());
    }
}
