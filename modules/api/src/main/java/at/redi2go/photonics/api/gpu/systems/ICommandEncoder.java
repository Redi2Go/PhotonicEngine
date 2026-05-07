package at.redi2go.photonics.api.gpu.systems;

import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import at.redi2go.photonics.api.gpu.buffers.IGpuBufferSlice;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture2D;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture3D;
import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.nio.ByteBuffer;

public interface ICommandEncoder {
    void clearColorTexture(IGpuTexture<?> gpuTexture, Vector4fc clearColor);

    void writeToBuffer(IGpuBuffer buffer, ByteBuffer byteBuffer);

    void writeToBuffer(IGpuBufferSlice slice, ByteBuffer byteBuffer);

    IGpuBuffer.MappedView mapBuffer(IGpuBuffer buffer, boolean readable, boolean writeable);

    IGpuBuffer.MappedView mapBuffer(IGpuBufferSlice bufferSlice, boolean readable, boolean writeable);

    void copyToBuffer(IGpuBufferSlice slice1, IGpuBufferSlice slice2);

    void writeToTexture(
            IGpuTexture2D texture,
            ByteBuffer data,
            Vector2ic offset,
            Vector2ic size
    );

    void writeToTexture(
            IGpuTexture3D texture,
            ByteBuffer data,
            Vector3ic offset,
            Vector3ic size
    );
}
