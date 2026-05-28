package at.redi2go.photonics.impl.mc.blaze3d.opengl.textures;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import at.redi2go.photonics.api.gpu.textures.TextureUsage;
import at.redi2go.photonics.impl.mc.blaze3d.opengl.GlDebugLabelExt;
import com.mojang.blaze3d.opengl.GlStateManager;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import org.jetbrains.annotations.NonNls;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public abstract class AbstractGlTexture<D> implements Disposable, IGlTexture {
    private final boolean hasUserLabel;
    private @NonNls String label;

    @TextureUsage
    protected final int usage;
    protected final InternalTextureFormat textureFormat;
    protected final int mipLevels;

    private boolean closed = false;
    private int handle = 0;

    protected D size;

    public AbstractGlTexture(
            @Nullable Supplier<String> label,
            int usage,
            ITextureFormat textureFormat,
            D size,
            int mipLevels
    ) {
        this.hasUserLabel = label != null;
        this.label = label == null ? null : label.get();
        this.usage = usage;
        this.textureFormat = (InternalTextureFormat) (Object) textureFormat;
        this.mipLevels = mipLevels;

        this.size = copySize(size);
        createTextureObject();
    }

    private void createTextureObject() {
        int handle = GlStateManager._genTexture();
        initTexture(handle);

        this.handle = handle;

        if (!hasUserLabel) label = String.valueOf(handle);
        GlDebugLabelExt.getInstance().applyLabel(this);
    }

    protected abstract void initTexture(int handle);

    protected abstract D copySize(D value);

    @Override
    public int handle() {
        return handle;
    }

    public String ph$label() {
        return label;
    }

    @TextureUsage
    public int ph$usage() {
        return usage;
    }

    public int ph$mipLevels() {
        return mipLevels;
    }

    public ITextureFormat ph$format() {
        return (ITextureFormat) (Object) textureFormat;
    }

    public void ph$resize(D newSize) {
        if (closed) throw new IllegalStateException("closed");
        if (size.equals(newSize)) return;

        this.size = copySize(newSize);

        destroyTexture();
        createTextureObject();
    }

    public boolean ph$isClosed() {
        return closed;
    }

    private void destroyTexture() {
        if (handle != 0) {
            GlStateManager._deleteTexture(handle);
            handle = 0;
        }
    }

    @Override
    public void close() {
        if (closed) return;

        destroyTexture();
        closed = true;
    }
}
