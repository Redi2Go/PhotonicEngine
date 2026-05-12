package at.redi2go.photonics.core.iris;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.systems.IRenderSystem;
import at.redi2go.photonics.api.mc.Minecraft;
import at.redi2go.photonics.api.shaders.PhotonicsProperties;
import at.redi2go.photonics.core.iris.pipeline.buffer.IBufferHolder;
import at.redi2go.photonics.core.iris.pipeline.texture.ISamplerHolder;
import at.redi2go.photonics.core.iris.pipeline.uniform.IDynamicUniformHolder;
import at.redi2go.photonics.core.iris.pipeline.uniform.IUniformHolder;
import at.redi2go.photonics.core.rendering.RenderingComponent;
import at.redi2go.photonics.core.rendering.SectionManager;
import at.redi2go.photonics.core.rendering.lights.BufferLightList;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;
import at.redi2go.photonics.core.rendering.world.block.palette.buffer.BufferPaletteTexture;
import at.redi2go.photonics.core.rendering.world.compiler.ChunkCompiler;
import at.redi2go.photonics.core.rendering.world.compiler.WorldCompiler;
import at.redi2go.photonics.core.rendering.world.registry.buffer.BufferBlockRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPhotonicsExtension implements PhotonicsExtension {
    private static final int ROOT_VOXEL_DEPTH = 3;

    protected final PhotonicsProperties properties;

    private final List<RenderingComponent> components;
    private final List<Disposable> resources = new ArrayList<>();

    public AbstractPhotonicsExtension(
            PhotonicsProperties properties,
            AtlasDownloader atlasDownloader,
            @Nullable RenderingComponent... components
    ) {
        this.properties = properties;
        this.components = new ArrayList<>(components.length);

        for (var component : components) {
            if (component != null) {
                this.components.add(component);
            }
        }

        registerResource(atlasDownloader);
        var sectionManager = registerComponent(new SectionManager(Minecraft::getRenderDistance));

        var heap = registerResource(IRenderSystem.getDevice().createBufferHeap(
                () -> "Photonics World Buffer",
                1 << 29,
                0
        ));

        var paletteTexture = registerComponent(new BufferPaletteTexture(2048, 600));
        var blockRegistry = registerComponent(new BufferBlockRegistry(heap, paletteTexture));

        var builtSectionQueue = sectionManager.<ChunkCompiler.BuildResult>newTaskQueue(WorldCompiler.MAX_SECTIONS_PER_RUN << 1);
        var worldCompiler = registerComponent(new WorldCompiler(
                ROOT_VOXEL_DEPTH,
                sectionManager,
                builtSectionQueue,
                heap,
                paletteTexture,
                blockRegistry
        ));

        registerComponent(new ChunkCompiler(
                sectionManager,
                builtSectionQueue,
                atlasDownloader,
                blockRegistry
        ));

        registerComponent(
                new BufferLightList(
                        sectionManager,
                        properties.getMaxLights(),
                        worldCompiler::origin
                )
        );
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
