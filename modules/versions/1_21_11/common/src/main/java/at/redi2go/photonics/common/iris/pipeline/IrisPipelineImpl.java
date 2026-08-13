package at.redi2go.photonics.common.iris.pipeline;

import at.redi2go.photonics.common.iris.pipeline.builder.IrisPipelineBuilderImpl;
import at.redi2go.photonics.common.iris.pipeline.framebuffer.FramebufferSize;
import at.redi2go.photonics.common.iris.pipeline.framebuffer.IrisFramebufferBuilderImpl;
import at.redi2go.photonics.common.iris.pipeline.renderer.IrisRendererBuilder;
import at.redi2go.photonics.common.iris.pipeline.renderer.DeferredIrisRenderer;
import at.redi2go.photonics.core.iris.pipeline.IrisPipeline;
import at.redi2go.photonics.core.iris.pipeline.IrisRenderer;
import at.redi2go.photonics.core.iris.pipeline.buffer.IBufferHolder;
import at.redi2go.photonics.core.iris.pipeline.texture.ISamplerHolder;
import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import at.redi2go.photonics.core.iris.pipeline.uniform.IDynamicUniformHolder;
import at.redi2go.photonics.core.iris.pipeline.uniform.IUniformHolder;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class IrisPipelineImpl implements IrisPipeline {
    private final List<Consumer<IBufferHolder>> buffers;
    private final List<Consumer<ISamplerHolder>> samplers;
    private final List<Consumer<IDynamicUniformHolder>> dynamicUniforms;
    private final List<Consumer<IUniformHolder>> uniforms;

    private final List<DeferredIrisRenderer> commonRenderers;

    public IrisPipelineImpl(List<DeferredIrisRenderer> commonRenderers) {
        this.buffers = new ArrayList<>();
        this.samplers = new ArrayList<>();
        this.dynamicUniforms = new ArrayList<>();
        this.uniforms = new ArrayList<>();

        this.commonRenderers = commonRenderers;
    }


    @Override
    public IrisPipeline withBuffer(Consumer<IBufferHolder> consumer) {
        buffers.add(consumer);

        return this;
    }

    @Override
    public void registerBuffers(IBufferHolder buffers) {
        this.buffers.forEach(e -> e.accept(buffers));
    }

    @Override
    public IrisPipeline withSampler(Consumer<ISamplerHolder> consumer) {
        samplers.add(consumer);

        return this;
    }

    @Override
    public void registerCustomTextures(ISamplerHolder samplers) {
        this.samplers.forEach(e -> e.accept(samplers));
    }

    @Override
    public IrisPipeline withDynamicUniform(Consumer<IDynamicUniformHolder> consumer) {
        dynamicUniforms.add(consumer);

        return this;
    }

    @Override
    public void registerDynamicUniforms(IDynamicUniformHolder dynamicUniforms) {
        this.dynamicUniforms.forEach(e -> e.accept(dynamicUniforms));
    }

    @Override
    public IrisPipeline withUniform(Consumer<IUniformHolder> consumer) {
        uniforms.add(consumer);

        return this;
    }

    @Override
    public void registerUniforms(IUniformHolder uniforms) {
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
