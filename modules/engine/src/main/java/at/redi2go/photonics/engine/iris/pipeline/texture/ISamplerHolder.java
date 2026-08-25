package at.redi2go.photonics.engine.iris.pipeline.texture;

import at.redi2go.photonics.game.blaze3d.textures.IGpuTexture;

import java.util.function.Supplier;

public interface ISamplerHolder {
    void addSampler(String name, Supplier<IGpuTexture.WithSampler> textureAndSampler);

    void addDefaultSampler(String name, Supplier<IGpuTexture> texture);
}
