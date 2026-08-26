package at.redi2go.photonics.common.iris.pipeline;

import at.redi2go.photonics.common.iris.pipeline.builder.IrisPipelineBuilderImpl;
import at.redi2go.photonics.common.iris.pipeline.renderer.IrisRendererBuilder;
import at.redi2go.photonics.common.iris.pipeline.renderer.DeferredIrisPassAction;
import at.redi2go.photonics.common.iris.pipeline.textures.FramebufferSize;
import at.redi2go.photonics.common.iris.pipeline.textures.IrisFramebufferBuilderImpl;
import at.redi2go.photonics.engine.iris.pipeline.IrisPipeline;
import at.redi2go.photonics.engine.iris.pipeline.IrisRenderer;
import at.redi2go.photonics.engine.iris.pipeline.buffers.IrisBufferHolder;
import at.redi2go.photonics.engine.iris.pipeline.textures.IrisFramebuffer;
import at.redi2go.photonics.engine.iris.pipeline.textures.IrisSamplerHolder;
import at.redi2go.photonics.engine.iris.pipeline.uniforms.IrisDynamicUniformHolder;
import at.redi2go.photonics.engine.iris.pipeline.uniforms.IrisUniformHolder;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class IrisPipelineImpl implements IrisPipeline {
    private final List<Consumer<IrisBufferHolder>> buffers;
    private final List<Consumer<IrisSamplerHolder>> samplers;
    private final List<Consumer<IrisDynamicUniformHolder>> dynamicUniforms;
    private final List<Consumer<IrisUniformHolder>> uniforms;

    private final List<DeferredIrisPassAction> commonRenderers;

    public IrisPipelineImpl(List<DeferredIrisPassAction> commonRenderers) {
        this.buffers = new ArrayList<>();
        this.samplers = new ArrayList<>();
        this.dynamicUniforms = new ArrayList<>();
        this.uniforms = new ArrayList<>();

        this.commonRenderers = commonRenderers;
    }


    @Override
    public IrisPipeline withBuffer(Consumer<IrisBufferHolder> consumer) {
        buffers.add(consumer);

        return this;
    }

    @Override
    public void registerBuffers(IrisBufferHolder buffers) {
        this.buffers.forEach(e -> e.accept(buffers));
    }

    @Override
    public IrisPipeline withSampler(Consumer<IrisSamplerHolder> consumer) {
        samplers.add(consumer);

        return this;
    }

    @Override
    public void registerCustomTextures(IrisSamplerHolder samplers) {
        this.samplers.forEach(e -> e.accept(samplers));
    }

    @Override
    public IrisPipeline withDynamicUniform(Consumer<IrisDynamicUniformHolder> consumer) {
        dynamicUniforms.add(consumer);

        return this;
    }

    @Override
    public void registerDynamicUniforms(IrisDynamicUniformHolder dynamicUniforms) {
        this.dynamicUniforms.forEach(e -> e.accept(dynamicUniforms));
    }

    @Override
    public IrisPipeline withUniform(Consumer<IrisUniformHolder> consumer) {
        uniforms.add(consumer);

        return this;
    }

    @Override
    public void registerUniforms(IrisUniformHolder uniforms) {
        this.uniforms.forEach(e -> e.accept(uniforms));
    }

    @Override
    public IrisFramebuffer.Builder newFramebuffer(int width, int height) {
        return new IrisFramebufferBuilderImpl(
                new FramebufferSize.Fixed(width, height)
        );
    }

    @Override
    public IrisFramebuffer.Builder newFramebuffer(float widthScale, float heightScale) {
        return new IrisFramebufferBuilderImpl(
                new FramebufferSize.Relative(widthScale, heightScale)
        );
    }

    public IrisRendererBuilder newRenderer(@NonNls String name) {
        Objects.requireNonNull(name, "name");
        return new IrisRendererBuilder(name, commonRenderers);
    }

    @Override
    public IrisRenderer.Builder newRenderer() {
        return new IrisPipelineBuilderImpl(this);
    }
}
