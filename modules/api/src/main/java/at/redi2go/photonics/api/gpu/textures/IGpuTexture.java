package at.redi2go.photonics.api.gpu.textures;

import at.redi2go.photonics.api.Disposable;

public sealed interface IGpuTexture<D> extends Disposable permits IGpuTexture2D, IGpuTexture3D {
    String label();

    @TextureUsage int usage();

    int mipLevels();

    ITextureFormat format();

    D size(int mipLevel);

    default D size() {
        return size(0);
    }

    void resize(D newSize);

    default WithSampler<D> withSampler(IGpuSampler sampler) {
        if (isClosed()) throw new IllegalStateException("closed");

        return new WithSampler<>(this, sampler);
    }

    boolean isClosed();

    record WithSampler<D>(IGpuTexture<D> texture, IGpuSampler sampler) {

    }
}
