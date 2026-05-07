package at.redi2go.photonics.impl.mixins.mc.blaze3d.common.textures;

import at.redi2go.photonics.api.gpu.textures.IFilterMode;
import com.mojang.blaze3d.textures.FilterMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(IFilterMode.class)
public interface IFilterModeImpl {
    @Overwrite
    static IFilterMode nearest() {
        return (IFilterMode) (Object) FilterMode.NEAREST;
    }

    @Overwrite
    static IFilterMode linear() {
        return (IFilterMode) (Object) FilterMode.LINEAR;
    }
}
