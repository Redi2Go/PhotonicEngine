package at.redi2go.photonics.core.iris;

import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.systems.IRenderSystem;
import at.redi2go.photonics.api.mc.Minecraft;
import at.redi2go.photonics.api.shaders.PhotonicsProperties;
import at.redi2go.photonics.core.iris.pipeline.buffer.IBufferHolder;
import at.redi2go.photonics.core.iris.pipeline.uniform.IUniformHolder;
import at.redi2go.photonics.core.iris.pipeline.uniform.IUniformUpdateFrequency;
import at.redi2go.photonics.core.rendering.SectionCopy;
import at.redi2go.photonics.core.rendering.world.BlockRegistry;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;
import at.redi2go.photonics.core.rendering.world.block.palette.buffer.BufferPaletteAllocator;
import at.redi2go.photonics.core.rendering.world.compiler.ChunkCompiler;
import at.redi2go.photonics.core.rendering.SectionManager;
import at.redi2go.photonics.core.rendering.world.compiler.WorldCompiler;
import at.redi2go.photonics.core.rendering.world.registry.buffer.BufferBlockRegistry;
import org.joml.Vector3d;
import org.joml.Vector3i;

public abstract class AbstractPhotonicsExtension implements PhotonicsExtension {
    private static final int ROOT_VOXEL_DEPTH = 3;

    protected final PhotonicsProperties properties;
    private final AtlasDownloader atlasDownloader;

    private IGpuBufferHeap heap;
    private BufferPaletteAllocator paletteTexture;

    private BlockRegistry registry;

    private final SectionManager sectionManager;

    private final WorldCompiler worldCompiler;
    private final ChunkCompiler chunkCompiler;

    public AbstractPhotonicsExtension(PhotonicsProperties properties, AtlasDownloader atlasDownloader) {
        this.properties = properties;
        this.atlasDownloader = atlasDownloader;

        this.heap = IRenderSystem.getDevice()
                .createBufferHeap(
                        () -> "Photonics World Buffer",
                        1 << 29,
                        0
                );

        this.paletteTexture = new BufferPaletteAllocator(2048, 600);
        this.registry = new BufferBlockRegistry(heap, paletteTexture);

        this.sectionManager = new SectionManager(Minecraft::getRenderDistance);
        var builtSectionQueue = sectionManager.<ChunkCompiler.BuildResult>newTaskQueue(WorldCompiler.MAX_SECTIONS_PER_RUN << 1);

        this.worldCompiler = new WorldCompiler(
                ROOT_VOXEL_DEPTH,
                Minecraft::getRenderDistance,
                sectionManager,
                builtSectionQueue,
                registry,
                heap
        );

        this.chunkCompiler = new ChunkCompiler(
                sectionManager,
                builtSectionQueue,
                atlasDownloader,
                registry
        );
    }

    @Override
    public void onFrameBegin() {
        worldCompiler.doUpload(() -> {
            heap.upload();
            paletteTexture.upload();
        });

        try {
            sectionManager.updateRenderDistance();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onSectionAdded(int x, int y, int z) {
        try {
            sectionManager.submitNewSection(new Vector3i(x, y, z));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onSectionChanged(int x, int y, int z) {
        try {
            sectionManager.submitRebuild(new Vector3i(x, y, z));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerUniforms(IUniformHolder uniforms) {
        uniforms.uniform3d(IUniformUpdateFrequency.perFrame(), "world_offset", () -> {
            var offset = worldCompiler.origin();
            if (offset == null) return new Vector3d(0f);

            return new Vector3d(offset);
        });

        uniforms.uniform3f(IUniformUpdateFrequency.perFrame(), "world_min_voxel", worldCompiler::minVoxel);
        uniforms.uniform3f(IUniformUpdateFrequency.perFrame(), "world_max_voxel", worldCompiler::maxVoxel);

        uniforms.uniform3d(IUniformUpdateFrequency.perFrame(), "rt_camera_position", () -> {
            var offset = worldCompiler.origin();
            if (offset == null) return new Vector3d(0f);

            var pos = Minecraft.getCameraPos();
            return offset.applyOffset(new Vector3d(pos.x, pos.y, pos.z));
        });
    }

    @Override
    public void registerBuffers(IBufferHolder buffers) {
        buffers.addDefaultBufferHeap("world_voxel_buffer", () -> heap);
        buffers.addDefaultBufferHeap("palette_texture", () -> paletteTexture.heap());
    }

    @Override
    public void close() {
        worldCompiler.close();
        chunkCompiler.close();

        registry.close();

        atlasDownloader.close();
        heap.close();
        paletteTexture.close();
    }
}
