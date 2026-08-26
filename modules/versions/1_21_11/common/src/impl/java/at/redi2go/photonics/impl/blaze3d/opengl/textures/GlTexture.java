package at.redi2go.photonics.impl.blaze3d.opengl.textures;

import at.redi2go.photonics.game.blaze3d.buffers.IGpuBuffer;
import at.redi2go.photonics.game.blaze3d.textures.IGpuTexture;
import at.redi2go.photonics.game.blaze3d.textures.TextureFormat;
import at.redi2go.photonics.game.blaze3d.textures.TextureUsage;
import at.redi2go.photonics.impl.blaze3d.opengl.DirectStateAccessor;
import at.redi2go.photonics.impl.blaze3d.opengl.GlObject;
import com.mojang.blaze3d.GpuOutOfMemoryException;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector2ic;
import org.joml.Vector3ic;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.nio.ByteBuffer;
import java.util.Objects;

public sealed abstract class GlTexture implements IGpuTexture, GlObject permits GlTexture1D, GlTexture2D, GlTexture3D, GlTextureCubemap {
    protected final int handle;
    private boolean closed;

    protected final String label;
    protected final @TextureUsage int usage;
    protected final TextureFormat format;

    protected int width;
    protected int height;
    protected int depthOrLayers;
    protected final int mipLevels;

    GlTexture(
            @Nullable String label,
            @TextureUsage int usage,
            TextureFormat format,
            Vector3ic size,
            int mipLevels
    ) {
        Objects.requireNonNull(format, "format");
        checkArguments(size, mipLevels);

        GlStateManager.clearGlErrors();
        this.handle = GlStateManager._genTexture();
        this.label = Objects.requireNonNullElseGet(label, () -> String.valueOf(handle));
        this.usage = usage;
        this.format = format;

        this.width = size.x();
        this.height = size.y();
        this.depthOrLayers = size.z();
        this.mipLevels = mipLevels;

        this.closed = false;

        int err = GlStateManager._getError();
        if (err != 0)
            throw new IllegalStateException("OpenGL error " + err);
    }

    protected abstract String printDimensions(Vector3ic size);

    protected GlTextureStateAccess createState() {
        return new StateAccessImpl();
    }

    public abstract int getTarget();

    protected void bind() {
        GL11.glBindTexture(getTarget(), handle);
    }

    protected void unbind() {

    }

    protected void texParameterImpl(int pname, int param) {
        GlStateManager._texParameter(getTarget(), pname, param);
    }

    protected abstract void texImageImpl(int layer, int mipLevel, @Nullable ByteBuffer pixels);

    protected abstract void texSubDataImpl(ByteBuffer byteBuffer, TextureFormat bufferFormat, int layer, int mipLevel, Vector3ic size, Vector3ic offset);

    public GlTextureStateAccess createStateAccess() {
        RenderSystem.assertOnRenderThread();
        checkNotClosed();

        return createState();
    }

    @Override
    public int ph$getHandle() {
        return handle;
    }

    @Override
    public @TextureUsage int ph$usage() {
        return usage;
    }

    @Override
    public String ph$getLabel() {
        return label;
    }

    @Override
    public TextureFormat ph$getFormat() {
        return format;
    }

    @Override
    public int ph$getWidth(int mipLevel) {
        return width >> mipLevel;
    }

    @Override
    public int ph$getHeight(int mipLevel) {
        return height >> mipLevel;
    }

    @Override
    public void ph$resize(Vector3ic newSize) {
        if (ph$getSize(0).equals(newSize)) return;

        try(var state = createStateAccess()) {
            state.texParameter(GL12.GL_TEXTURE_MIN_LOD, 0);
            state.texParameter(GL12.GL_TEXTURE_MAX_LOD, mipLevels - 1);
            state.texParameter(GL12.GL_TEXTURE_MAX_LEVEL, mipLevels - 1);

            for (int layer = 0; layer < ph$getLayers(); layer++) {
                for (int mip = 0; mip < mipLevels; mip++)
                    state.texImage(layer, mip, ph$getSize(mipLevels), null);
            }
        }
    }

    @Override
    public int ph$getMipLevels() {
        return mipLevels;
    }


    @Override
    public boolean ph$isClosed() {
        return closed;
    }

    protected void checkNotClosed() {
        if (closed)
            throw new IllegalStateException("closed");
    }

    @Override
    public void close() {
        RenderSystem.assertOnRenderThread();
        if (!closed) {
            closed = true;
            GlStateManager._deleteTexture(this.handle);
        }
    }

    protected class StateAccessImpl implements GlTextureStateAccess {
        private boolean bound;

        protected StateAccessImpl() {
            GlStateManager.clearGlErrors();

            bind();
            bound = true;
        }

        protected void checkCanUse() {
            RenderSystem.assertOnRenderThread();
            checkNotClosed();
        }

        protected void checkLayersRange(int layer) {
            if (layer > ph$getLayers())
                throw new UnsupportedOperationException("Layer is out of range, must be >= 0 and < " + ph$getLayers());
        }

        protected void checkMipLevelRange(int mipLevel) {
            if (mipLevel > ph$getMipLevels())
                throw new IllegalArgumentException("Invalid mipLevel, must be >= 0 and < " + ph$getMipLevels());
        }

        @Override
        public void texParameter(int pname, int param) {
            checkCanUse();
            texParameterImpl(pname, param);
        }

        @Override
        public void texImage(int layer, int mipLevel, Vector3ic size, @Nullable ByteBuffer pixels) {
            checkCanUse();
            checkMipLevelRange(mipLevel);
            checkLayersRange(layer);

            boolean resizeable = (usage & TextureUsage.RESIZEABLE) != 0;
            if (resizeable) checkArguments(size, mipLevels);
            if (!resizeable && !ph$getSize(0).equals(size)) throw new IllegalArgumentException("Only textures with RESIZEABLE can be resized");

            int requiredByteSize = size.x() * size.y() * size.z() * format.getTexelByteSize();
            if (pixels != null && requiredByteSize > pixels.remaining())
                throw new IllegalArgumentException(String.format(
                        "Copy would overrun the source buffer (remaining length of %s, but write is %s of format %s (%s bytes))",
                        pixels.remaining(),
                        printDimensions(size),
                        format,
                        requiredByteSize
                ));

            width = size.x();
            height = size.y();
            depthOrLayers = size.z();

            texImageImpl(layer, mipLevel, pixels);
        }

        @Override
        public void texSubData(ByteBuffer byteBuffer, TextureFormat bufferFormat, int layer, int mipLevel, Vector3ic size, Vector3ic offset) {
            checkCanUse();
            checkMipLevelRange(mipLevel);
            checkLayersRange(layer);

            if (!bufferFormat.supportsWrite()) throw new IllegalArgumentException("Format of source buffer (" + bufferFormat + ") cannot be used for a write");
            if ((usage & TextureUsage.COPY_DST) == 0) throw new IllegalStateException("Color texture must have COPY_DST to be a destination for a write");

            if (size.x() < 1) throw new IllegalArgumentException("width must be at least 1 (was " + size.x() + ")");
            if (size.y() < 1) throw new IllegalArgumentException("height must be at least 1 (was " + size.y() + ")");
            if (size.z() < 1) throw new IllegalArgumentException("depth must be at least 1 (was " + size.z() + ")");

            if (offset.x() < 1) throw new IllegalArgumentException("x offset must be at least 1 (was " + offset.x() + ")");
            if (offset.y() < 1) throw new IllegalArgumentException("y offset must be at least 1 (was " + offset.y() + ")");
            if (offset.z() < 1) throw new IllegalArgumentException("z offset must be at least 1 (was " + offset.z() + ")");

            if (!contains(mipLevel, size, offset)) {
                throw new IllegalArgumentException(String.format(
                        "Dest texture (%s) is not large enough to write a cuboid of %s at %s",
                        printDimensions(ph$getSize(mipLevel)),
                        printDimensions(size),
                        printDimensions(offset)
                ));
            }

            int requiredByteSize = size.x() * size.y() * size.z() * bufferFormat.getTexelByteSize();
            if (requiredByteSize > byteBuffer.remaining())
                throw new IllegalArgumentException(String.format(
                        "Copy would overrun the source buffer (remaining length of %s, but copy is %s of format %s (%s bytes))",
                        byteBuffer.remaining(),
                        printDimensions(size),
                        bufferFormat,
                        requiredByteSize
                ));

            texSubDataImpl(byteBuffer, bufferFormat, mipLevel, layer, size, offset);
        }

        @Override
        public void texReadPixels(int readFbo, DirectStateAccessor dsa, Vector2ic srcOffset, Vector2ic copySize, IGpuBuffer dstBuffer, long dstOffset, int layer, int mipLevel, Runnable completionCallback) {
            checkCanUse();

            throw new UnsupportedOperationException("copy not supported by " + label);
        }

        @Override
        public void close() {
            checkCanUse();
            if (bound) {
                bound = false;
                unbind();

                int err = GlStateManager._getError();
                switch (err) {
                    case GL11.GL_NO_ERROR -> {
                    }

                    case GL11.GL_OUT_OF_MEMORY ->
                            throw new GpuOutOfMemoryException("Could not allocate texture of " + printDimensions(ph$getSize(0)) + " for " + label);

                    default -> throw new IllegalStateException("OpenGL error " + err);
                }
            }
        }
    }

    protected void checkArguments(Vector3ic size, int mipLevels) {
        if (size.x() < 1) throw new IllegalArgumentException("width must be at least 1 (was " + size.x() + ")");
        if (size.y() < 1) throw new IllegalArgumentException("height must be at least 1 (was " + size.y() + ")");
        if (size.z() < 1) throw new IllegalArgumentException("depthOrLayers must be at least 1 (was " + size.z() + ")");
        if (mipLevels < 1) throw new IllegalArgumentException("mipLevels must be at least 1 (was " + mipLevels + ")");
    }

    public static GlTexture createTexture(
            @Nullable String label,
            @TextureUsage int usage,
            TextureFormat textureFormat,
            Vector3ic size,
            int mipLevels
    ) {
        RenderSystem.assertOnRenderThread();

        if ((usage & TextureUsage.CUBEMAP_COMPATIBLE) != 0)
            return new GlTextureCubemap(label, usage, textureFormat, size, mipLevels);
        if (size.z() > 1) return new GlTexture3D(label, usage, textureFormat, size, mipLevels);
        if (size.y() > 1) return new GlTexture2D(label, usage, textureFormat, size, mipLevels);

        return new GlTexture1D(label, usage, textureFormat, size, mipLevels);
    }
}
