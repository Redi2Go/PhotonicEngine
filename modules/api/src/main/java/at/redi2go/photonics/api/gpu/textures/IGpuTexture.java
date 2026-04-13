package at.redi2go.photonics.api.gpu.textures;

import at.redi2go.photonics.api.Disposable;

public interface IGpuTexture extends Disposable {
    int getWidth(int mipLevel);

    int getHeight(int mipLevel);

    int getDepthOrLayers();

    int mipLevels();

    ITextureFormat format();

    @TextureUsage int usage();

    String label();

    boolean isClosed();
}
