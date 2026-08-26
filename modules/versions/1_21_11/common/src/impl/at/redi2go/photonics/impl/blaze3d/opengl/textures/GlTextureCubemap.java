package at.redi2go.photonics.impl.blaze3d.opengl.textures;

import at.redi2go.photonics.game.blaze3d.textures.TextureFormat;
import at.redi2go.photonics.game.blaze3d.textures.TextureUsage;
import at.redi2go.photonics.impl.blaze3d.opengl.GlConstExt;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import org.joml.Vector3ic;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.nio.ByteBuffer;

public final class GlTextureCubemap extends GlTexture {
    GlTextureCubemap(
            @Nullable String label,
            @TextureUsage int usage,
            TextureFormat format,
            Vector3ic size,
            int mipLevels
    ) {
        super(label, usage, format, size, mipLevels);
    }

    @Override
    protected String printDimensions(Vector3ic size) {
        return "%sx%s".formatted(width, height);
    }

    @Override
    protected void checkArguments(Vector3ic size, int mipLevels) {
        super.checkArguments(size, mipLevels);

        if (size.x() != size.y()) throw new IllegalArgumentException("Cubemap compatible textures must be square, but size is " + size.x() + "x" + size.y());
        if (size.z() % 6 != 0) throw new IllegalArgumentException("Cubemap compatible textures must have a layer count with a multiple of 6, was " + size.z());
        if (size.z() > 6) throw new UnsupportedOperationException("Array textures are not yet supported");
    }

    @Override
    public int getTarget() {
        return GL13.GL_TEXTURE_CUBE_MAP;
    }

    @Override
    public int ph$getLayers() {
        return depthOrLayers;
    }

    @Override
    public int ph$getDepth(int mipLevel) {
        return 1;
    }

    @Override
    protected void texImageImpl(int layer, int mipLevel, @Nullable ByteBuffer pixels) {
        GlStateManager._texImage2D(
                GlConst.CUBEMAP_TARGETS[layer],
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
                GlConst.CUBEMAP_TARGETS[layer],
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
}
