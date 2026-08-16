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

    boolean ph$isClosed();
}
