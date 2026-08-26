package at.redi2go.photonics.engine.iris.pipeline.textures;

import at.redi2go.photonics.game.blaze3d.textures.IGpuTexture;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ISamplerHolderBuilder<T> {
    T withSampler(Consumer<IrisSamplerHolder> consumer);

    default T sampler(String name, Supplier<IGpuTexture.WithSampler> textureAndSampler) {
        return withSampler(samplers -> samplers.addSampler(name, textureAndSampler));
    }

    default T defaultSampler(String name, Supplier<IGpuTexture> texture) {
        return withSampler(samplers -> samplers.addDefaultSampler(name, texture));
    }
}
