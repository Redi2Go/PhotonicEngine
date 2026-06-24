package at.redi2go.photonics.core.iris;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.mc.Minecraft;
import at.redi2go.photonics.api.shaders.LightingMode;
import at.redi2go.photonics.api.shaders.PhotonicsProperties;
import at.redi2go.photonics.core.iris.pipeline.buffer.IBufferHolder;
import at.redi2go.photonics.core.iris.pipeline.rendering.IrisPipeline;
import at.redi2go.photonics.core.iris.pipeline.texture.ISamplerHolder;
import at.redi2go.photonics.core.iris.pipeline.uniform.IDynamicUniformHolder;
import at.redi2go.photonics.core.iris.pipeline.uniform.IUniformHolder;
import at.redi2go.photonics.core.rendering.AbstractRenderingComponent;
import at.redi2go.photonics.core.rendering.HandheldLightComponent;
import at.redi2go.photonics.core.rendering.RenderingComponent;
import at.redi2go.photonics.core.rendering.SectionManager;
import at.redi2go.photonics.core.rendering.lights.BufferLightList;
import at.redi2go.photonics.core.rendering.lights.HandheldItemSupplier;
import at.redi2go.photonics.core.rendering.world.allocator.buffer.BufferPaletteTexture;
import at.redi2go.photonics.core.rendering.world.allocator.buffer.BufferWorldAllocator;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;
import at.redi2go.photonics.core.rendering.world.compiler.ChunkCompiler;
import at.redi2go.photonics.core.rendering.world.compiler.WorldCompiler;
import at.redi2go.photonics.core.rendering.world.registry.WorldRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPhotonicsExtension extends AbstractRenderingComponent implements PhotonicsExtension {
    private static final int ROOT_VOXEL_DEPTH = 3;

    protected final PhotonicsProperties properties;
    private final List<IrisPipeline> renderers = new ArrayList<>();

    public AbstractPhotonicsExtension(
            PhotonicsProperties properties,
            AtlasDownloader atlasDownloader,
            HandheldItemSupplier handheldItemSupplier,
            @Nullable RenderingComponent... components
    ) {
        super(components);
        this.properties = properties;

        registerResource(atlasDownloader);
        var sectionManager = registerComponent(new SectionManager(Minecraft::getRenderDistance));

        var worldAllocator = registerComponent(new BufferWorldAllocator(1 << 29));
        var paletteTexture = registerComponent(new BufferPaletteTexture(2048, 600));

        var worldRegistry = new WorldRegistry(worldAllocator, paletteTexture, atlasDownloader);

        var builtSectionQueue = sectionManager.<ChunkCompiler.BuildResult>newTaskQueue(WorldCompiler.MAX_SECTIONS_PER_RUN << 1, true);
        var worldCompiler = registerComponent(new WorldCompiler(
                ROOT_VOXEL_DEPTH,
                worldAllocator,
                paletteTexture,
                builtSectionQueue,
                worldRegistry
        ));

        registerComponent(new ChunkCompiler(
                sectionManager,
                builtSectionQueue,
                worldRegistry
        ));

        registerComponent(
                new BufferLightList(
                        sectionManager,
                        properties.getMaxLights(),
                        worldCompiler::origin
                )
        );

        if (properties.getLightingMode() != LightingMode.OFF && properties.isHandheldLightEnabled())
            registerComponent(
                    new HandheldLightComponent(
                            handheldItemSupplier,
                            properties
                    )
            );
    }

    public <T extends IrisPipeline> T registerRenderer(T component) {
        if (component != null) {
            if (component instanceof RenderingComponent renderingComponent)
                registerComponent(renderingComponent);

            renderers.add(component);
        }

        return component;
    }

    @Override
    public void onRender() {
        renderers.forEach(IrisPipeline::renderAll);
    }
}
