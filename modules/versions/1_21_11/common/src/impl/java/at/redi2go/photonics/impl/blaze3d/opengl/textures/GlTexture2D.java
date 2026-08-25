package at.redi2go.photonics.impl.blaze3d.opengl.textures;

import at.redi2go.photonics.game.blaze3d.buffers.BufferUsage;
import at.redi2go.photonics.game.blaze3d.buffers.IGpuBuffer;
import at.redi2go.photonics.game.blaze3d.textures.TextureFormat;
import at.redi2go.photonics.game.blaze3d.textures.TextureUsage;
import at.redi2go.photonics.impl.blaze3d.opengl.DirectStateAccessor;
import at.redi2go.photonics.impl.blaze3d.opengl.GlConstExt;
import at.redi2go.photonics.impl.blaze3d.opengl.buffers.GlBufferAccessor;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector2ic;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

public final class GlTexture2D extends GlTexture {
    GlTexture2D(
            @Nullable String label,
            @TextureUsage int usage,
            TextureFormat format,
            Vector3ic size,
            int mipLevels
    ) {
        super(label, usage, format, size, mipLevels);
    }

    @Override
    public int ph$getLayers() {
        return 1;
    }

    @Override
    public int ph$getDepth(int mipLevel) {
        return 1;
    }

    @Override
    protected GlTextureStateAccess createState() {
        return new StateAccess2D();
    }

    @Override
    protected String printDimensions(Vector3ic size) {
        return "%sx%s".formatted(size.x(), size.y());
    }

    @Override
    public int getTarget() {
        return GL11.GL_TEXTURE_2D;
    }

    @Override
    public void bind() {
        GlStateManager._bindTexture(id);
    }

    @Override
    protected void texImageImpl(int layer, int mipLevel, @Nullable ByteBuffer pixels) {
        GlStateManager._texImage2D(
                GL11.GL_TEXTURE_2D,
                mipLevel,
                GlConstExt.toGlInternalId(format),
                ph$getWidth(mipLevel),
                ph$getHeight(mipLevel),
                0,
                GlConstExt.toGlTexelFormatId(format.getTexelFormat()),
                GlConstExt.toGlTexelTypeId(format.getTexelType()),
                pixels
        );
    }

    @Override
    protected void texSubDataImpl(ByteBuffer byteBuffer, TextureFormat bufferFormat, int layer, int mipLevel, Vector3ic size, Vector3ic offset) {
        GlStateManager._pixelStore(GL11.GL_PACK_ROW_LENGTH, size.x());
        GlStateManager._pixelStore(GL11.GL_UNPACK_SKIP_PIXELS, 0);
        GlStateManager._pixelStore(GL11.GL_UNPACK_SKIP_ROWS, 0);
        GlStateManager._pixelStore(GL11.GL_UNPACK_ALIGNMENT, bufferFormat.getTexelFormat().getComponentCount());

        GL11.glTexSubImage2D(
                GL11.GL_TEXTURE_2D,
                mipLevel,
                offset.x(),
                offset.y(),
                size.x(),
                size.y(),
                GlConstExt.toGlTexelFormatId(bufferFormat.getTexelFormat()),
                GlConstExt.toGlTexelTypeId(bufferFormat.getTexelType()),
                byteBuffer
        );
    }

    private class StateAccess2D extends StateAccessImpl {
        @Override
        public void texReadPixels(
                int readFbo,
                DirectStateAccessor dsa,
                Vector2ic srcOffset,
                Vector2ic copySize,
                IGpuBuffer dstBuffer,
                long dstOffset,
                int layer,
                int mipLevel,
                Runnable completionCallback
        ) {
            checkCanUse();
            checkMipLevelRange(mipLevel);
            checkLayersRange(layer);

            if (!format.supportsWrite()) throw new IllegalArgumentException("Format of texture (" + format + ") cannot be used for a copy");
            if ((usage & TextureUsage.COPY_SRC) == 0) throw new IllegalStateException("Texture needs COPY_SRC to be a source for a copy");
            if ((dstBuffer.ph$usage() & BufferUsage.COPY_DST) == 0) throw new IllegalArgumentException("Buffer needs COPY_DST to be a destination for a copy");
            if (dstBuffer.ph$isClosed()) throw new IllegalStateException("Destination buffer is closed");

            if (copySize.x() < 1) throw new IllegalArgumentException("copy width must be at least 1 (was " + copySize.x() + ")");
            if (copySize.y() < 1) throw new IllegalArgumentException("copy height must be at least 1 (was " + copySize.y() + ")");

            if (srcOffset.x() < 1) throw new IllegalArgumentException("x offset must be at least 1 (was " + srcOffset.x() + ")");
            if (srcOffset.y() < 1) throw new IllegalArgumentException("y offset must be at least 1 (was " + srcOffset.y() + ")");

            if (!contains(mipLevel, new Vector3i(copySize, 1), new Vector3i(srcOffset, 0))) {
                throw new IllegalArgumentException(String.format(
                        "Copy source texture (%s) is not large enough to copy a rectangle of %s at %s",
                        printDimensions(ph$getSize(mipLevel)),
                        printDimensions(new Vector3i(copySize, 1)),
                        printDimensions(new Vector3i(srcOffset, 0))
                ));
            }

            long copyByteSize = (long) copySize.x() * copySize.y() * format.getTexelByteSize();
            long requiredByteSize = copyByteSize + dstOffset;
            if (requiredByteSize > dstBuffer.ph$size())
                throw new IllegalArgumentException(String.format(
                        "Buffer of size %s is not large enough to hold %s pixels (%s bytes) starting from offset %s",
                        dstBuffer.ph$size(),
                        printDimensions(new Vector3i(copySize, 1)),
                        copyByteSize,
                        dstOffset
                ));

            GlStateManager.clearGlErrors();
            dsa.invokeBindFrameBufferTextures(readFbo, id, 0, mipLevel, GL30.GL_READ_FRAMEBUFFER);
            GlStateManager._glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, ((GlBufferAccessor) dstBuffer).getHandle());
            GlStateManager._pixelStore(GL11.GL_PACK_ROW_LENGTH, copySize.x());

            GlStateManager._readPixels(
                    srcOffset.x(),
                    srcOffset.y(),
                    copySize.x(),
                    copySize.y(),
                    GlConstExt.toGlTexelFormatId(format.getTexelFormat()),
                    GlConstExt.toGlTexelTypeId(format.getTexelType()),
                    dstOffset
            );

            RenderSystem.queueFencedTask(completionCallback);
            GlStateManager._glFramebufferTexture2D(GL30.GL_READ_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, 0, mipLevel);
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
            GlStateManager._glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, 0);
            int o = GlStateManager._getError();
            if (o != 0) {
                throw new IllegalStateException("Couldn't perform copyTobuffer for texture " + label + ": GL error " + o);
            }
        }
    }
}
