package at.redi2go.photonics.common.mixins.iris.extension;

import at.redi2go.photonics.common.iris.IrisUtil;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSectionManager.class)
public abstract class RenderSectionManagerMixin {
    @Inject(method = "onSectionAdded", at = @At("HEAD"))
    private void onSectionAdded(int x, int y, int z, CallbackInfo ci) {
        IrisUtil.getPhotonics()
                .ifPresent(photonics -> photonics.onSectionLoad(x, y, z));
    }

    @Inject(method = "onSectionRemoved", at = @At("HEAD"))
    private void onSectionRemoved(int x, int y, int z, CallbackInfo ci) {
        IrisUtil.getPhotonics()
                .ifPresent(photonics -> photonics.onSectionUnload(x, y, z));
    }

    @Inject(method = "scheduleRebuild", at = @At("HEAD"))
    private void scheduleRebuild(int x, int y, int z, boolean playerChanged, CallbackInfo ci) {
        IrisUtil.getPhotonics()
                .ifPresent(photonics -> photonics.onSectionLoad(x, y, z));
    }
}
