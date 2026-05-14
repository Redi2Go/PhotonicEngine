package at.redi2go.photonics.core.rendering.lights;

import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.api.gpu.systems.IRenderSystem;
import at.redi2go.photonics.core.iris.pipeline.buffer.IBufferHolder;
import at.redi2go.photonics.core.rendering.SectionManager;
import at.redi2go.photonics.core.rendering.world.WorldOrigin;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

public class BufferLightList extends AbstractLightList {
    // 12 floats per light struct (4 bytes per float)
    // position (vec3f) + block id, color (vec3f) + intensity, attenuation (vec2f) + falloff + radius in blocks
    private static final int LIGHT_DATA_SIZE = 12;
    private static final int LIGHT_BYTE_SIZE = LIGHT_DATA_SIZE * 4;

    private final IGpuBufferHeap listHeap;
    private final MemoryView listView;

    private final IGpuBufferHeap mappingHeap;
    private final MemoryView mappingView;

    public BufferLightList(
            SectionManager sectionManager,
            int maxLights,
            Supplier<WorldOrigin> worldOriginSupplier
    ) {
        super(sectionManager, maxLights, worldOriginSupplier);

        this.listHeap = IRenderSystem.getDevice()
                .createBufferHeap(
                        () -> "Photonics Light List",
                        (long) maxLights * LIGHT_BYTE_SIZE,
                        0
                );

        this.listView = listHeap.allocateOrThrow(listHeap.capacity());


        this.mappingHeap = IRenderSystem.getDevice()
                .createBufferHeap(
                        () -> "Photonics Light Mapping",
                        (long) maxLights * 4,
                        0
                );

        this.mappingView = mappingHeap.allocateOrThrow(mappingHeap.capacity());
    }

    @Override
    protected void storeLight(int index, Vector4f[] light) {
        ByteBuffer buffer = listView.buffer().position(index * LIGHT_BYTE_SIZE);

        for (var vec : light) {
            buffer.putFloat(vec.x);
            buffer.putFloat(vec.y);
            buffer.putFloat(vec.z);
            buffer.putFloat(vec.w);
        }
    }

    @Override
    protected void storeMapping(int beforeIndex, int afterIndex) {
        mappingView.buffer().putInt(beforeIndex * 4, afterIndex);
    }

    @Override
    protected void clearMapping() {
        for (int i = 0; i < mostRecentLights.size(); i++) {
            mappingView.buffer().putInt(i * 4, i);
        }

        mappingView.upload();
    }

    @Override
    protected void prepareUpload() {
        listView.upload();
        mappingView.upload();
    }

    @Override
    protected void upload() {
        listHeap.upload();
        mappingHeap.upload();
    }

    @Override
    public void registerBuffers(IBufferHolder buffers) {
        buffers.addDefaultBufferHeap(
                "ph_light_list",
                () -> listHeap
        );

        buffers.addDefaultBufferHeap(
                "ph_light_mapping",
                () -> mappingHeap
        );
    }

    @Override
    public void close() {
        super.close();

        listHeap.close();
        mappingHeap.close();
    }
}
