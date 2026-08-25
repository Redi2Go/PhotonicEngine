package at.redi2go.photonics.impl.blaze3d.common.buffers;

import at.redi2go.photonics.game.blaze3d.buffers.IGpuBuffer;
import at.redi2go.photonics.game.blaze3d.buffers.IGpuBufferSlice;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteBuffer;

@Mixin(GpuBuffer.class)
public abstract class GpuBufferImpl implements IGpuBuffer {
    @Shadow
    public abstract int usage();

    @Shadow
    public abstract long size();

    @Shadow
    public abstract GpuBufferSlice slice(long l, long m);

    @Shadow
    public abstract GpuBufferSlice slice();

    @Shadow
    public abstract boolean isClosed();

    @Override
    public int ph$usage() {
        return usage();
    }

    @Override
    public long ph$size() {
        return size();
    }

    @Override
    public IGpuBufferSlice ph$slice(long offset, long length) {
        return (IGpuBufferSlice) (Object) slice(offset, length);
    }

    @Override
    public IGpuBufferSlice ph$slice() {
        return (IGpuBufferSlice) (Object) slice();
    }

    @Override
    public boolean ph$isClosed() {
        return isClosed();
    }

    @Mixin(GpuBuffer.MappedView.class)
    public interface MappedViewImpl extends IGpuBuffer.MappedView {
        @Shadow
        ByteBuffer data();

        @Override
        default ByteBuffer ph$data() {
            return data();
        }
    }
}
