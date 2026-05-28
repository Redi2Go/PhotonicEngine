package at.redi2go.photonics.api.gpu.textures;

import at.redi2go.photonics.api.Disposable;

public sealed interface IGpuTexture<D> extends Disposable permits IGpuTexture2D, IGpuTexture3D {
    String ph$label();

    @TextureUsage int ph$usage();

    int ph$mipLevels();

    ITextureFormat ph$format();

    D ph$size(int mipLevel);

    default D ph$size() {
        return ph$size(0);
    }

    void ph$resize(D newSize);

    default WithSampler<D> withSampler(IGpuSampler sampler) {
        if (ph$isClosed()) throw new IllegalStateException("closed");

        return new WithSampler<>(this, sampler);
    }

    boolean ph$isClosed();

    record WithSampler<D>(IGpuTexture<D> texture, IGpuSampler sampler) {

    }
}
