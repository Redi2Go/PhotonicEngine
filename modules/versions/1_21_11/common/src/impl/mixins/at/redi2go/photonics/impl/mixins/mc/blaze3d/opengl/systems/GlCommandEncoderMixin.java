package at.redi2go.photonics.impl.mixins.mc.blaze3d.opengl.systems;

import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import at.redi2go.photonics.api.gpu.buffers.IGpuBufferSlice;
import at.redi2go.photonics.api.gpu.systems.ICommandEncoder;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture2D;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture3D;
import at.redi2go.photonics.impl.mc.blaze3d.opengl.textures.IGlTexture;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.CommandEncoder;
import org.apache.commons.lang3.NotImplementedException;
import org.joml.Vector2ic;
import org.joml.Vector3ic;
import org.joml.Vector4fc;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.nio.ByteBuffer;

@Mixin(GlCommandEncoder.class)
@Implements(@Interface(iface = ICommandEncoder.class, prefix = "ph$"))
public abstract class GlCommandEncoderMixin implements CommandEncoder {
    @Shadow
    private boolean inRenderPass;

    @Shadow
    @Final
    private GlDevice device;

    @Shadow
    @Final
    private int drawFbo;

    @Unique
    private void checkNotInRenderPass() {
        if (inRenderPass)
            throw new IllegalStateException("Close the existing render pass before creating a new one!");
    }

    public void ph$clearColorTexture(IGpuTexture<?> gpuTexture, Vector4fc clearColor) {
        checkNotInRenderPass();

        ((DirectStateAccessor) device.directStateAccess()).invokeBindFrameBufferTextures(drawFbo, ((IGlTexture) gpuTexture).handle(), 0, 0, 36160);
        GL11.glClearColor(clearColor.x(), clearColor.y(), clearColor.z(), clearColor.w());
        GlStateManager._disableScissorTest();
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._clear(16384);
        GlStateManager._glFramebufferTexture2D(36160, 36064, 3553, 0, 0);
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    public void ph$writeToBuffer(IGpuBuffer buffer, ByteBuffer byteBuffer) {
        writeToBuffer(((GpuBuffer) buffer).slice(), byteBuffer);
    }

    public void ph$writeToBuffer(IGpuBufferSlice slice, ByteBuffer byteBuffer) {
        writeToBuffer((GpuBufferSlice) (Object) slice, byteBuffer);
    }

    public IGpuBuffer.MappedView ph$mapBuffer(IGpuBuffer buffer, boolean readable, boolean writeable) {
        return (IGpuBuffer.MappedView) mapBuffer((GpuBuffer) buffer, readable, writeable);
    }

    public IGpuBuffer.MappedView ph$mapBuffer(IGpuBufferSlice bufferSlice, boolean readable, boolean writeable) {
        return (IGpuBuffer.MappedView) mapBuffer((GpuBufferSlice) (Object) bufferSlice, readable, writeable);
    }

    public void ph$copyToBuffer(IGpuBufferSlice slice1, IGpuBufferSlice slice2) {
        copyToBuffer((GpuBufferSlice) (Object) slice1, (GpuBufferSlice) (Object) slice2);
    }

    public void ph$writeToTexture(
            IGpuTexture2D texture,
            ByteBuffer data,
            Vector2ic offset,
            Vector2ic size
    ) {
        throw new NotImplementedException("TODO");
    }

    public void ph$writeToTexture(IGpuTexture3D texture, ByteBuffer data, Vector3ic offset, Vector3ic size) {
        throw new NotImplementedException("TODO");
    }
}
