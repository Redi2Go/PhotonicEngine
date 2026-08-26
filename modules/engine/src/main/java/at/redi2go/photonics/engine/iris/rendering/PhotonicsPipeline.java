package at.redi2go.photonics.engine.iris.rendering;

import at.redi2go.photonics.engine.rendering.world.bakery.texture.AtlasDownloader;
import at.redi2go.photonics.engine.rendering.lights.HandheldItemSupplier;
import at.redi2go.photonics.engine.iris.pipeline.IrisPipeline;
import at.redi2go.photonics.engine.iris.pipeline.IrisRenderer;
import at.redi2go.photonics.engine.iris.properties.PhotonicsProperties;
import at.redi2go.photonics.engine.rendering.AbstractRenderingComponent;
import at.redi2go.photonics.engine.rendering.RenderingComponent;
//import at.redi2go.photonics.core.rendering.SectionManager;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class PhotonicsPipeline extends AbstractRenderingComponent {
    private static final int ROOT_VOXEL_DEPTH = 3;

    protected final PhotonicsProperties properties;
    private final List<IrisRenderer> renderers = new ArrayList<>();

    public PhotonicsPipeline(
            PhotonicsProperties properties,
            AtlasDownloader atlasDownloader,
            IrisPipeline pipeline,
            @Nullable RenderingComponent... components
    ) {
        super(components);
        this.properties = properties;
        registerComponent(pipeline);

        registerResource(atlasDownloader);
//        var sectionManager = registerComponent(new SectionManager(Minecraft::getRenderDistance));
//
//        var worldAllocator = registerComponent(new BufferWorldAllocator(1 << 29));
//        var paletteTexture = registerComponent(new BufferPaletteTexture(2048, 600));

//        var worldRegistry = new WorldRegistry(worldAllocator, paletteTexture, atlasDownloader);
//
//        var builtSectionQueue = sectionManager.<ChunkCompiler.BuildResult>newTaskQueue(WorldCompiler.MAX_SECTIONS_PER_RUN << 1, true);
//        var worldCompiler = registerComponent(new WorldCompiler(
//                ROOT_VOXEL_DEPTH,
//                worldAllocator,
//                paletteTexture,
//                builtSectionQueue,
//                worldRegistry
//        ));
//
//        registerComponent(new ChunkCompiler(
//                sectionManager,
//                builtSectionQueue,
//                worldRegistry
//        ));
//
//        registerComponent(
//                new BufferLightList(
//                        sectionManager,
//                        properties.getLightListProperties().getSize(),
//                        worldCompiler::origin
//                )
//        );
    }

    public <T extends IrisRenderer> T registerRenderer(T component) {
        if (component != null) {
            if (component instanceof RenderingComponent renderingComponent)
                registerComponent(renderingComponent);

            renderers.add(component);
        }

        return component;
    }

    public void onRender() {
        renderers.forEach(IrisRenderer::renderAll);
    }

    @FunctionalInterface
    interface Supplier<T extends PhotonicsPipeline, P> {
        T create(PhotonicsProperties phProperties, P rendererProperties, AtlasDownloader atlasDownloader, IrisPipeline irisPipeline);
    }
}
