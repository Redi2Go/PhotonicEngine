package at.redi2go.photonics.game.blaze3d.textures;

import at.redi2go.photonics.game.Disposable;
import org.joml.Vector3i;
import org.joml.Vector3ic;

public interface IGpuTexture extends Disposable {
    String ph$getLabel();

    @TextureUsage
    int ph$usage();

    TextureFormat ph$getFormat();

    int ph$getWidth(int mipLevel);

    default int ph$getWidth() {
        return ph$getWidth(0);
    }

    int ph$getHeight(int mipLevel);

    default int ph$getHeight() {
        return ph$getHeight(0);
    }

    int ph$getDepth(int mipLevel);

    default int ph$getDepth() {
        return ph$getDepth(0);
    }

    default Vector3ic ph$getSize(int mipLevel) {
        return new Vector3i(ph$getWidth(mipLevel), ph$getHeight(mipLevel), ph$getDepth(mipLevel));
    }

    int ph$getLayers();

    int ph$getMipLevels();

    default boolean contains(int mipLevel, Vector3ic size, Vector3ic offset) {
        return (size.x() + offset.x()) <= ph$getWidth(mipLevel) &&
                (size.y() + offset.y()) <= ph$getHeight(mipLevel) &&
                (size.z() + offset.z()) <= ph$getDepth(mipLevel);
    }

    default WithSampler withSampler(IGpuSampler sampler) {
        if (ph$isClosed()) throw new IllegalStateException("closed");

        return new WithSampler(this, sampler);
    }

    boolean ph$isClosed();


    record WithSampler(IGpuTexture texture, IGpuSampler sampler) {

    }
}
