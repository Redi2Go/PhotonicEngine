package at.redi2go.photonics.impl.blaze3d.opengl.textures;

import at.redi2go.photonics.game.blaze3d.textures.TextureFormat;
import at.redi2go.photonics.game.blaze3d.textures.TextureUsage;
import at.redi2go.photonics.impl.blaze3d.opengl.GlConstExt;
import com.mojang.blaze3d.opengl.GlStateManager;
import org.joml.Vector3ic;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

public final class GlTexture1D extends GlTexture {
    GlTexture1D(
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
    protected String printDimensions(Vector3ic size) {
        return "%s".formatted(size.x());
    }

    @Override
    public int getTarget() {
        return GL11.GL_TEXTURE_1D;
    }

    @Override
    protected void texImageImpl(int layer, int mipLevel, @Nullable ByteBuffer pixels) {
        GL11.glTexImage1D(
                GL11.GL_TEXTURE_1D,
                mipLevel,
                GlConstExt.toGlInternalId(format),
                ph$getWidth(mipLevel),
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

        GL11.glTexSubImage1D(
                GL11.GL_TEXTURE_1D,
                mipLevel,
                offset.x(),
                size.x(),
                GlConstExt.toGlTexelFormatId(bufferFormat.getTexelFormat()),
                GlConstExt.toGlTexelTypeId(bufferFormat.getTexelType()),
                byteBuffer
        );
    }
}
