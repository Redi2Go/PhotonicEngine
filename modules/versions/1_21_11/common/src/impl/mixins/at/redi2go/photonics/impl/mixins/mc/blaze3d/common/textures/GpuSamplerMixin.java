package at.redi2go.photonics.impl.mixins.mc.blaze3d.common.textures;

import at.redi2go.photonics.api.gpu.textures.IAddressMode;
import at.redi2go.photonics.api.gpu.textures.IFilterMode;
import at.redi2go.photonics.api.gpu.textures.IGpuSampler;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.OptionalDouble;

@Mixin(GpuSampler.class)
@Implements(@Interface(iface = IGpuSampler.class, prefix = "ph$"))
public abstract class GpuSamplerMixin implements IGpuSampler {
    @Shadow
    public abstract AddressMode getAddressModeU();

    public IAddressMode ph$addressModeU() {
        return (IAddressMode) (Object) getAddressModeU();
    }

    @Shadow
    public abstract AddressMode getAddressModeV();

    @Override
    public IAddressMode addressModeV() {
        return (IAddressMode) (Object) getAddressModeV();
    }

    @Shadow
    public abstract FilterMode getMinFilter();

    @Override
    public IFilterMode minFilter() {
        return (IFilterMode) (Object) getMinFilter();
    }

    @Shadow
    public abstract FilterMode getMagFilter();

    @Override
    public IFilterMode magFilter() {
        return (IFilterMode) (Object) getMagFilter();
    }

    @Shadow
    public abstract int getMaxAnisotropy();

    @Override
    public int maxAnisotropy() {
        return getMaxAnisotropy();
    }

    @Shadow
    public abstract OptionalDouble getMaxLod();

    @Override
    public OptionalDouble maxLod() {
        return getMaxLod();
    }
}
