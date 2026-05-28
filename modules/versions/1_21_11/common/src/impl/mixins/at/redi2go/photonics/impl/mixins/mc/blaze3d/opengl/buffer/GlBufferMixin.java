package at.redi2go.photonics.impl.mixins.mc.blaze3d.opengl.buffer;

import at.redi2go.photonics.api.gpu.buffers.BufferUsage;
import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import at.redi2go.photonics.api.gpu.buffers.IGpuBufferSlice;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlBuffer;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GlBuffer.class)
public abstract class GlBufferMixin extends GpuBuffer implements IGpuBuffer {
    public GlBufferMixin(@Usage int i, long l) {
        super(i, l);
    }

    @Override
    public long ph$size() {
        return size();
    }

    @Override
    public @BufferUsage int ph$usage() {
        return usage();
    }

    @Override
    public boolean ph$isClosed() {
        return isClosed();
    }

    @Override
    public IGpuBufferSlice ph$slice(long offset, long length) {
        return (IGpuBufferSlice) (Object) slice(offset, length);
    }
}
