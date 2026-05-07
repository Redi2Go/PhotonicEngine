package at.redi2go.photonics.impl.mc.blaze3d.opengl.textures;

import at.redi2go.photonics.api.gpu.textures.IGpuTexture2D;
import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import at.redi2go.photonics.api.gpu.textures.TextureUsage;
import at.redi2go.photonics.impl.mc.blaze3d.opengl.GlDebugLabelExt;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlSampler;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.SamplerCache;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import org.jetbrains.annotations.NonNls;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL42.glTexStorage2D;

public class GlTexture2D extends AbstractGlTexture<Vector2ic> implements IGpuTexture2D {
    public GlTexture2D(@Nullable Supplier<String> label, int usage, ITextureFormat textureFormat, Vector2ic size, int mipLevels) {
        super(label, usage, textureFormat, size, mipLevels);
    }

    @Override
    public Vector2ic size(int mipLevel) {
        return new Vector2i(
                size.x() >> mipLevel,
                size.y() >> mipLevel
        );
    }

    @Override
    protected void initTexture(int handle) {
        glBindTexture(GL_TEXTURE_2D, handle);
        glTexStorage2D(GL_TEXTURE_2D, mipLevels, textureFormat.getGlFormat(), size.x(), size.y());
    }

    @Override
    protected Vector2ic copySize(Vector2ic value) {
        return new Vector2i(value);
    }
}
