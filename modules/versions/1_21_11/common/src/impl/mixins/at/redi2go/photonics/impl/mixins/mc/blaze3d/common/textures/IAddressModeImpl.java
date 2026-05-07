package at.redi2go.photonics.impl.mixins.mc.blaze3d.common.textures;

import at.redi2go.photonics.api.gpu.textures.IAddressMode;
import com.mojang.blaze3d.textures.AddressMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(IAddressMode.class)
public interface IAddressModeImpl {
    @Overwrite
    static IAddressMode repeat() {
        return (IAddressMode) (Object) AddressMode.REPEAT;
    }

    @Overwrite
    static IAddressMode clampToEdge() {
        return (IAddressMode) (Object) AddressMode.CLAMP_TO_EDGE;
    }
}
