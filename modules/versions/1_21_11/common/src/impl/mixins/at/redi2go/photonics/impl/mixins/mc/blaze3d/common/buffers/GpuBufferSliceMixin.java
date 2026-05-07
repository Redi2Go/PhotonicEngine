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
@Implements(@Interface(iface = IGpuBufferSlice.class, prefix = "ph$"))
public abstract class GpuBufferSliceMixin {
    @Shadow public abstract GpuBuffer shadow$buffer();

    public IGpuBuffer ph$buffer() {
        return (IGpuBuffer) shadow$buffer();
    }

    @Shadow public abstract GpuBufferSlice shadow$slice(long l, long m);

    public IGpuBufferSlice ph$slice(long offset, long length) {
        return (IGpuBufferSlice) (Object) shadow$slice(offset, length);
    }
}
