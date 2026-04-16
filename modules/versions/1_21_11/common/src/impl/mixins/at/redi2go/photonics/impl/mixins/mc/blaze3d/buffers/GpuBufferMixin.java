package at.redi2go.photonics.impl.mixins.mc.blaze3d.buffers;

import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import at.redi2go.photonics.api.gpu.buffers.IGpuBufferSlice;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GpuBuffer.class)
public abstract class GpuBufferMixin implements IGpuBuffer {
    @Shadow public abstract GpuBufferSlice shadow$slice(long l, long m);

    @Override
    public IGpuBufferSlice slice(long offset, long length) {
        return (IGpuBufferSlice) (Object) shadow$slice(offset, length);
    }
}
