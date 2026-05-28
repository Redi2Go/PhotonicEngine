package at.redi2go.photonics.impl.mixins.mc.blaze3d.common.buffers;

import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import at.redi2go.photonics.api.gpu.buffers.IGpuBufferSlice;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GpuBufferSlice.class)
public abstract class GpuBufferSliceMixin implements IGpuBufferSlice {
    @Shadow public abstract GpuBuffer buffer();

    @Shadow public abstract long offset();

    @Shadow public abstract long length();

    @Shadow public abstract GpuBufferSlice slice(long l, long m);

    @Override
    public IGpuBuffer ph$buffer() {
        return (IGpuBuffer) buffer();
    }

    @Override
    public long ph$offset() {
        return offset();
    }

    @Override
    public long ph$length() {
        return length();
    }

    @Override
    public IGpuBufferSlice ph$slice(long offset, long length) {
        return (IGpuBufferSlice) (Object) slice(offset, length);
    }
}
