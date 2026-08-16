package at.redi2go.photonics.game.blaze3d.systems;

import at.redi2go.photonics.game.blaze3d.buffers.IGpuBuffer;
import at.redi2go.photonics.game.blaze3d.buffers.IGpuBufferSlice;
import at.redi2go.photonics.game.blaze3d.images.ClientImage;
import at.redi2go.photonics.game.blaze3d.images.IImageFormat;
import at.redi2go.photonics.game.blaze3d.textures.IGpuTexture;
import org.joml.Vector2ic;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.nio.ByteBuffer;

public interface ICommandEncoder {
    void ph$writeToBuffer(IGpuBufferSlice gpuBufferSlice, ByteBuffer byteBuffer);

    IGpuBuffer.MappedView ph$mapBuffer(IGpuBuffer gpuBuffer, boolean readable, boolean writable);

    IGpuBuffer.MappedView ph$mapBuffer(IGpuBufferSlice gpuBufferSlice, boolean readable, boolean writable);

    void ph$copyToBuffer(IGpuBufferSlice srcSlice, IGpuBufferSlice dstSlice);

    void ph$writeToTexture(
            IGpuTexture gpuTexture,
            ByteBuffer byteBuffer,
            IImageFormat format,
            int mipLevel,
            Vector3ic size,
            Vector3ic offset
    );

    default void ph$writeToTexture(
            IGpuTexture gpuTexture,
            ByteBuffer byteBuffer,
            IImageFormat format,
            int mipLevel,
            Vector2ic size,
            Vector2ic offset
    ) {
        ph$writeToTexture(
                gpuTexture,
                byteBuffer,
                format,
                mipLevel,
                new Vector3i(size, 1),
                new Vector3i(offset, 0)
        );
    }

    default void ph$writeToTexture(
            IGpuTexture gpuTexture,
            ClientImage image,
            int mipLevel,
            Vector3ic offset
    ) {
        ph$writeToTexture(
                gpuTexture,
                image.getStorage(),
                image.getFormat(),
                mipLevel,
                image.getSize(),
                offset
        );
    }

    default void ph$writeToTexture(
            IGpuTexture gpuTexture,
            ClientImage image,
            int mipLevel,
            Vector2ic offset
    ) {
        ph$writeToTexture(
                gpuTexture,
                image.getStorage(),
                image.getFormat(),
                mipLevel,
                image.getSize(),
                new Vector3i(offset, 0)
        );
    }


    void copyTextureToBuffer(IGpuTexture gpuTexture, IGpuBuffer gpuBuffer, long l, Runnable runnable, int i);

    void copyTextureToBuffer(IGpuTexture gpuTexture, IGpuBuffer gpuBuffer, long l, Runnable runnable, int i, int j, int k, int m, int n);

    void copyTextureToTexture(IGpuTexture gpuTexture, IGpuTexture gpuTexture2, int i, int j, int k, int l, int m, int n, int o);
}
