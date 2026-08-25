package at.redi2go.photonics.impl.blaze3d.opengl.textures;

import at.redi2go.photonics.game.Disposable;
import at.redi2go.photonics.game.blaze3d.buffers.IGpuBuffer;
import at.redi2go.photonics.game.blaze3d.textures.TextureFormat;
import at.redi2go.photonics.impl.blaze3d.opengl.DirectStateAccessor;
import org.joml.Vector2ic;
import org.joml.Vector3ic;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;

public interface GlTextureStateAccess extends Disposable {
    void texParameter(int pname, int param);

    void texImage(int layer, int mipLevel, @Nullable ByteBuffer pixels);

    void texSubData(ByteBuffer byteBuffer, TextureFormat bufferFormat, int layer, int mipLevel, Vector3ic size, Vector3ic offset);

    void texReadPixels(
            int readFbo,
            DirectStateAccessor dsa,
            Vector2ic srcOffset,
            Vector2ic copySize,
            IGpuBuffer dstBuffer,
            long dstOffset,
            int layer,
            int mipLevel,
            Runnable completionCallback
    );
}
