package at.redi2go.photonics.core.rendering;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.shaders.PhotonicsProperties;
import at.redi2go.photonics.core.iris.pipeline.buffer.IBufferHolder;
import at.redi2go.photonics.core.iris.pipeline.texture.ISamplerHolder;
import at.redi2go.photonics.core.iris.pipeline.uniform.IDynamicUniformHolder;
import at.redi2go.photonics.core.iris.pipeline.uniform.IUniformHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRenderingComponent implements RenderingComponent {
    private final List<RenderingComponent> components;
    private final List<Disposable> resources = new ArrayList<>();

    public AbstractRenderingComponent(@Nullable RenderingComponent... components) {
        this.components = new ArrayList<>(components.length);

        for (var component : components) {
            if (component != null) {
                this.components.add(component);
            }
        }
    }

    protected <T extends RenderingComponent> T registerComponent(T component) {
        if (component != null)
            this.components.add(component);

        return component;
    }

    protected <T extends Disposable> T registerResource(T resource) {
        if (resource != null)
            this.resources.add(resource);

        return resource;
    }

    @Override
    public void onFrameBegin() {
        components.forEach(RenderingComponent::onFrameBegin);
    }

    @Override
    public void onSectionAdded(int x, int y, int z) {
        for (var component : components)
            component.onSectionAdded(x, y, z);
    }

    @Override
    public void onSectionChanged(int x, int y, int z) {
        for (var component : components)
            component.onSectionChanged(x, y, z);
    }

    @Override
    public void registerUniforms(IUniformHolder uniforms) {
        for (var component : components)
            component.registerUniforms(uniforms);
    }

    @Override
    public void registerDynamicUniforms(IDynamicUniformHolder dynamicUniforms) {
        for (var component : components)
            component.registerDynamicUniforms(dynamicUniforms);
    }

    @Override
    public void registerBuffers(IBufferHolder buffers) {
        for (var component : components)
            component.registerBuffers(buffers);
    }

    @Override
    public void registerCustomTextures(ISamplerHolder samplers) {
        for (var component : components)
            component.registerCustomTextures(samplers);
    }

    @Override
    public void close() {
        for (int i = components.size() - 1; i >= 0; i--)
            components.get(i).close();

        for (int i = resources.size() - 1; i >= 0; i--)
            resources.get(i).close();

        components.clear();
        resources.clear();
    }

}
