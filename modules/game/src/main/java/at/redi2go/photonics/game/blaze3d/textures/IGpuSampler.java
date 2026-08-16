package at.redi2go.photonics.game.blaze3d.textures;

import at.redi2go.photonics.game.Disposable;

import java.util.OptionalDouble;

public interface IGpuSampler extends Disposable {
    IAddressMode ph$addressModeU();

    IAddressMode ph$addressModeV();

    IFilterMode ph$minFilter();

    IFilterMode ph$magFilter();

    int ph$maxAnisotropy();

    OptionalDouble ph$maxLod();
}
