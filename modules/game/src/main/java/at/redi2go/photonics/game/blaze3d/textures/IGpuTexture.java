package at.redi2go.photonics.game.blaze3d.textures;

import at.redi2go.photonics.game.Disposable;

public interface IGpuTexture extends Disposable {
    String ph$getLabel();

    @TextureUsage
    int ph$usage();

    ITextureFormat ph$getFormat();

    int ph$getWidth(int mipLevel);

    int ph$getHeight(int mipLevel);

    int ph$getDepthOrLayers();

    int ph$getMipLevels();

    default WithSampler withSampler(IGpuSampler sampler) {
        if (ph$isClosed()) throw new IllegalStateException("closed");

        return new WithSampler(this, sampler);
    }

    boolean ph$isClosed();


    record WithSampler(IGpuTexture texture, IGpuSampler sampler) {

    }
}
