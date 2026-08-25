package at.redi2go.photonics.game.blaze3d.systems;

import at.redi2go.photonics.game.blaze3d.buffers.BufferUsage;
import at.redi2go.photonics.game.blaze3d.buffers.IGpuBuffer;
import at.redi2go.photonics.game.blaze3d.buffers.IGpuBufferSlice;
import at.redi2go.photonics.game.blaze3d.textures.ClientImage;
import at.redi2go.photonics.game.blaze3d.textures.IGpuTexture;
import at.redi2go.photonics.game.blaze3d.textures.TextureFormat;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public interface ICommandEncoder {
    void ph$writeToBuffer(IGpuBufferSlice gpuBufferSlice, ByteBuffer byteBuffer);

    IGpuBuffer.MappedView ph$mapBuffer(IGpuBuffer gpuBuffer, @BufferUsage int mapUsage);

    IGpuBuffer.MappedView ph$mapBuffer(IGpuBufferSlice gpuBufferSlice, @BufferUsage int mapUsage);

    void ph$copyToBuffer(IGpuBufferSlice srcSlice, IGpuBufferSlice dstSlice);

    void ph$writeToTexture(
            IGpuTexture gpuTexture,
            ByteBuffer byteBuffer,
            TextureFormat bufferFormat,
            int layer,
            int mipLevel,
            Vector3ic size,
            Vector3ic offset
    );

    default void ph$writeToTexture(
            IGpuTexture gpuTexture,
            ByteBuffer byteBuffer,
            TextureFormat bufferFormat,
            int layer,
            int mipLevel,
            Vector2ic size,
            Vector2ic offset
    ) {
        ph$writeToTexture(
                gpuTexture,
                byteBuffer,
                bufferFormat,
                layer,
                mipLevel,
                new Vector3i(size, 1),
                new Vector3i(offset, 0)
        );
    }

    default void ph$writeToTexture(
            IGpuTexture gpuTexture,
            ByteBuffer byteBuffer,
            TextureFormat bufferFormat,
            int layer,
            int mipLevel,
            int size,
            int offset
    ) {
        ph$writeToTexture(
                gpuTexture,
                byteBuffer,
                bufferFormat,
                layer,
                mipLevel,
                new Vector3i(size, 1, 1),
                new Vector3i(offset, 0, 0)
        );
    }

    default void ph$writeToTexture(
            IGpuTexture gpuTexture,
            ClientImage image,
            int layer,
            int mipLevel,
            Vector3ic offset
    ) {
        ph$writeToTexture(
                gpuTexture,
                image.getStorage(),
                image.getFormat(),
                layer,
                mipLevel,
                image.getSize(),
                offset
        );
    }

    default void ph$writeToTexture(
            IGpuTexture gpuTexture,
            ClientImage image,
            int layer,
            int mipLevel,
            Vector2ic offset
    ) {
        ph$writeToTexture(
                gpuTexture,
                image.getStorage(),
                image.getFormat(),
                layer,
                mipLevel,
                image.getSize(),
                new Vector3i(offset, 0)
        );
    }

    default void ph$writeToTexture(
            IGpuTexture gpuTexture,
            ClientImage image,
            int layer,
            int mipLevel,
            int offset
    ) {
        ph$writeToTexture(
                gpuTexture,
                image.getStorage(),
                image.getFormat(),
                layer,
                mipLevel,
                image.getSize(),
                new Vector3i(offset, 0, 0)
        );
    }

    CompletableFuture<Void> ph$copyTextureToBuffer(
            IGpuTexture srcTexture,
            Vector2ic srcOffset,
            Vector2ic copySize,
            IGpuBuffer dstBuffer,
            long dstOffset,
            int layer,
            int mipLevel
    );

    default CompletableFuture<Void> ph$copyTextureToBuffer(
            IGpuTexture srcTexture,
            IGpuBuffer dstBuffer,
            long dstOffset,
            int layer,
            int mipLevel
    ) {
        return ph$copyTextureToBuffer(
                srcTexture,
                new Vector2i(0, 0),
                new Vector2i(srcTexture.ph$getWidth(mipLevel), srcTexture.ph$getHeight(mipLevel)),
                dstBuffer,
                dstOffset,
                layer,
                mipLevel
        );
    }

//TODO Maybe implement this
//    void ph$copyTextureToTexture(
//            IGpuTexture srcTexture,
//            Vector2ic srcOffset,
//            Vector2ic copySize,
//            IGpuTexture dstTexture,
//            Vector2ic dstOffset,
//            int layer,
//            int mipLevel
//    );
}
