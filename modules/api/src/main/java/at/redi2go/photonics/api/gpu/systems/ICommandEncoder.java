package at.redi2go.photonics.api.gpu.systems;

import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import at.redi2go.photonics.api.gpu.buffers.IGpuBufferSlice;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture2D;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture3D;
import org.joml.Vector2ic;
import org.joml.Vector3ic;
import org.joml.Vector4fc;

import java.nio.ByteBuffer;

public interface ICommandEncoder {
    void ph$clearColorTexture(IGpuTexture<?> gpuTexture, Vector4fc clearColor);

    void ph$writeToBuffer(IGpuBuffer buffer, ByteBuffer byteBuffer);

    void ph$writeToBuffer(IGpuBufferSlice slice, ByteBuffer byteBuffer);

    IGpuBuffer.MappedView ph$mapBuffer(IGpuBuffer buffer, boolean readable, boolean writeable);

    IGpuBuffer.MappedView ph$mapBuffer(IGpuBufferSlice bufferSlice, boolean readable, boolean writeable);

    void ph$copyToBuffer(IGpuBufferSlice slice1, IGpuBufferSlice slice2);

    void ph$writeToTexture(
            IGpuTexture2D texture,
            ByteBuffer data,
            Vector2ic offset,
            Vector2ic size
    );

    void ph$writeToTexture(
            IGpuTexture3D texture,
            ByteBuffer data,
            Vector3ic offset,
            Vector3ic size
    );
}
