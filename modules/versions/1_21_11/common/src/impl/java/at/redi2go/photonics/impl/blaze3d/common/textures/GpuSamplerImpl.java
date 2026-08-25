package at.redi2go.photonics.impl.blaze3d.common.textures;

import at.redi2go.photonics.game.blaze3d.textures.IAddressMode;
import at.redi2go.photonics.game.blaze3d.textures.IFilterMode;
import at.redi2go.photonics.game.blaze3d.textures.IGpuSampler;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.OptionalDouble;

@Mixin(GpuSampler.class)
public abstract class GpuSamplerImpl implements IGpuSampler {
    @Shadow
    public abstract AddressMode getAddressModeU();

    @Shadow
    public abstract AddressMode getAddressModeV();

    @Shadow
    public abstract FilterMode getMinFilter();

    @Shadow
    public abstract FilterMode getMagFilter();

    @Shadow
    public abstract int getMaxAnisotropy();

    @Shadow
    public abstract OptionalDouble getMaxLod();

    @Override
    public IAddressMode ph$addressModeU() {
        return (IAddressMode) (Object) getAddressModeU();
    }

    @Override
    public IAddressMode ph$addressModeV() {
        return (IAddressMode) (Object) getAddressModeV();
    }

    @Override
    public IFilterMode ph$minFilter() {
        return (IFilterMode) (Object) getMinFilter();
    }

    @Override
    public IFilterMode ph$magFilter() {
        return (IFilterMode) (Object) getMagFilter();
    }

    @Override
    public int ph$maxAnisotropy() {
        return getMaxAnisotropy();
    }

    @Override
    public OptionalDouble ph$maxLod() {
        return getMaxLod();
    }
}
