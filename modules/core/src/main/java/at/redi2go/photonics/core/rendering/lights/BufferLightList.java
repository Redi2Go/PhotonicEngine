package at.redi2go.photonics.core.rendering.lights;

import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.api.gpu.systems.IRenderSystem;
import at.redi2go.photonics.core.iris.pipeline.buffer.IBufferHolder;
import at.redi2go.photonics.core.rendering.SectionManager;
import at.redi2go.photonics.core.rendering.UniformUpdater;
import at.redi2go.photonics.core.rendering.world.WorldOrigin;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.function.Supplier;

public class BufferLightList extends AbstractLightList<MemoryView> {
    // 12 floats per light struct (4 bytes per float)
    // position (vec3f) + block id, color (vec3f) + intensity, attenuation (vec2f) + falloff + radius in blocks
    private static final int LIGHT_DATA_SIZE = 12;
    private static final int LIGHT_BYTE_SIZE = LIGHT_DATA_SIZE * 4;

    private final IGpuBufferHeap heap;
    private final MemoryView memoryView;

    public BufferLightList(
            SectionManager sectionManager,
            int maxLights,
            Supplier<WorldOrigin> worldOriginSupplier
    ) {
        super(sectionManager, maxLights, worldOriginSupplier);

        this.heap = IRenderSystem.getDevice()
                .createBufferHeap(
                        () -> "Photonics Light List",
                        (long) maxLights * LIGHT_BYTE_SIZE,
                        0
                );

        this.memoryView = heap.allocateOrThrow(heap.capacity());
    }

    @Override
    protected MemoryView getStorage() {
        return memoryView;
    }

    @Override
    protected void storeLight(MemoryView storage, int index, Vector4f[] light) {
        ByteBuffer buffer = storage.buffer().position(index * LIGHT_BYTE_SIZE);

        for (var vec : light) {
            buffer.putFloat(vec.x);
            buffer.putFloat(vec.y);
            buffer.putFloat(vec.z);
            buffer.putFloat(vec.w);
        }
    }

    @Override
    protected void markForUpload(MemoryView storage) {
        storage.upload();
    }

    @Override
    protected void upload() {
        heap.upload();
    }

    @Override
    public void registerBuffers(IBufferHolder buffers) {
        buffers.addDefaultBufferHeap(
            "ph_light_list",
                () -> heap
        );
    }

    @Override
    public void close() {
        super.close();

        heap.close();
    }
}
