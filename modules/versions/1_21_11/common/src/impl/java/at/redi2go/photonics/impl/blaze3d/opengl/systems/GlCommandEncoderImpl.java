package at.redi2go.photonics.impl.blaze3d.opengl.systems;

import at.redi2go.photonics.game.blaze3d.buffers.BufferUsage;
import at.redi2go.photonics.game.blaze3d.buffers.IGpuBuffer;
import at.redi2go.photonics.game.blaze3d.buffers.IGpuBufferSlice;
import at.redi2go.photonics.game.blaze3d.systems.ICommandEncoder;
import at.redi2go.photonics.game.blaze3d.textures.IGpuTexture;
import at.redi2go.photonics.game.blaze3d.textures.TextureFormat;
import at.redi2go.photonics.impl.blaze3d.opengl.DirectStateAccessor;
import at.redi2go.photonics.impl.blaze3d.opengl.textures.GlTexture;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.opengl.GlDevice;
import org.joml.Vector2ic;
import org.joml.Vector3ic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

@Mixin(GlCommandEncoder.class)
public abstract class GlCommandEncoderImpl implements ICommandEncoder {
    @Shadow
    public abstract void writeToBuffer(GpuBufferSlice gpuBufferSlice, ByteBuffer byteBuffer);

    @Shadow
    public abstract GpuBuffer.MappedView mapBuffer(GpuBuffer gpuBuffer, boolean bl, boolean bl2);

    @Shadow
    public abstract GpuBuffer.MappedView mapBuffer(GpuBufferSlice gpuBuffer, boolean bl, boolean bl2);

    @Shadow
    public abstract void copyToBuffer(GpuBufferSlice gpuBufferSlice, GpuBufferSlice gpuBufferSlice2);

    @Shadow private boolean inRenderPass;

    @Shadow @Final private int readFbo;

    @Shadow @Final private GlDevice device;

    @Unique
    private void checkNotInRenderPass() {
        if (inRenderPass)
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void ph$writeToBuffer(IGpuBufferSlice gpuBufferSlice, ByteBuffer byteBuffer) {
        writeToBuffer((GpuBufferSlice) (Object) gpuBufferSlice, byteBuffer);
    }

    @Override
    public IGpuBuffer.MappedView ph$mapBuffer(IGpuBuffer gpuBuffer, @BufferUsage int mapUsage) {
        if ((mapUsage & (BufferUsage.MAP_READ | BufferUsage.MAP_WRITE)) == 0)
            throw new IllegalArgumentException("Must have one of BufferUsage.MAP_READ or BufferUsage.MAP_WRITE");

        return (IGpuBuffer.MappedView) mapBuffer((GpuBuffer) gpuBuffer, (mapUsage & BufferUsage.MAP_READ) != 0, (mapUsage & BufferUsage.MAP_WRITE) != 0);
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public IGpuBuffer.MappedView ph$mapBuffer(IGpuBufferSlice gpuBufferSlice, @BufferUsage int mapUsage) {
        if ((mapUsage & (BufferUsage.MAP_READ | BufferUsage.MAP_WRITE)) == 0)
            throw new IllegalArgumentException("Must have one of BufferUsage.MAP_READ or BufferUsage.MAP_WRITE");

        return (IGpuBuffer.MappedView) mapBuffer((GpuBufferSlice) (Object) gpuBufferSlice, (mapUsage & BufferUsage.MAP_READ) != 0, (mapUsage & BufferUsage.MAP_WRITE) != 0);
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void ph$copyToBuffer(IGpuBufferSlice srcSlice, IGpuBufferSlice dstSlice) {
        copyToBuffer((GpuBufferSlice) (Object) srcSlice, (GpuBufferSlice) (Object) dstSlice);
    }

    @Override
    public void ph$writeToTexture(
            IGpuTexture gpuTexture,
            ByteBuffer byteBuffer,
            TextureFormat bufferFormat,
            int layer,
            int mipLevel,
            Vector3ic size,
            Vector3ic offset
    ) {
        checkNotInRenderPass();
        var texture = (GlTexture) gpuTexture;

        try (var state = texture.createStateAccess()) {
            state.texSubData(
                    byteBuffer,
                    bufferFormat,
                    layer,
                    mipLevel,
                    size,
                    offset
            );
        }
    }

    @Override
    public CompletableFuture<Void> ph$copyTextureToBuffer(
            IGpuTexture srcTexture,
            Vector2ic srcOffset,
            Vector2ic copySize,
            IGpuBuffer dstBuffer,
            long dstOffset,
            int layer,
            int mipLevel
    ) {
        checkNotInRenderPass();
        var future = new CompletableFuture<Void>();
        var texture = (GlTexture) srcTexture;

        try(var state = texture.createStateAccess()) {
            state.texReadPixels(
                    readFbo,
                    (DirectStateAccessor) device.directStateAccess(),
                    srcOffset,
                    copySize,
                    dstBuffer,
                    dstOffset,
                    layer,
                    mipLevel,
                    () -> future.complete(null)
            );
        } catch (Throwable e) {
            future.completeExceptionally(e);
        }

        return future;
    }
}
