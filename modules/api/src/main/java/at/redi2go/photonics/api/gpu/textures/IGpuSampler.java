package at.redi2go.photonics.api.gpu.textures;

import at.redi2go.photonics.api.Disposable;

import java.util.OptionalDouble;

public interface IGpuSampler extends Disposable {
    IAddressMode addressModeU();

    IAddressMode addressModeV();

    IFilterMode minFilter();

    IFilterMode magFilter();

    int maxAnisotropy();

    OptionalDouble maxLod();
}
