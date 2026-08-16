package at.redi2go.photonics.core.iris.pipeline.texture;

import at.redi2go.photonics.game.gpu.textures.IGpuTexture;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ISamplerHolderBuilder<T> {
    T withSampler(Consumer<ISamplerHolder> consumer);

    default T sampler(String name, Supplier<IGpuTexture.WithSampler<?>> textureAndSampler) {
        return withSampler(samplers -> samplers.addSampler(name, textureAndSampler));
    }

    default T defaultSampler(String name, Supplier<IGpuTexture<?>> texture) {
        return withSampler(samplers -> samplers.addDefaultSampler(name, texture));
    }
}
