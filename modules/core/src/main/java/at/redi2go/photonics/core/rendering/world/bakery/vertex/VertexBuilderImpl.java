package at.redi2go.photonics.core.rendering.world.bakery.vertex;

import at.redi2go.photonics.api.mc.Id;
import at.redi2go.photonics.core.rendering.world.bakery.VertexBuilder;
import at.redi2go.photonics.core.rendering.world.bakery.texture.CpuTexture;
import at.redi2go.photonics.core.rendering.world.block.VoxelColor;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class VertexBuilderImpl implements VertexBuilder {
    private static final int FP_ZERO = Float.floatToRawIntBits(0);

    private int[] vertexData = new int[1024];

    private int index = 0;
    private int size = 0;

    public void clear() {
        index = 0;
        size = 0;
    }

    public boolean hasNext() {
        return index < size;
    }

    public CpuTexture currentTexture() {
        throw new NotImplementedException("TODO");
    }

    public int currentBlockId() {
        throw new NotImplementedException("TODO");
    }

    public int readInt() {
        if (index >= size)
            throw new ArrayIndexOutOfBoundsException(index + " out of bounds for " + size);

        return vertexData[index++];
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    private void requireCapacity(int newSize) {
        if (newSize >= vertexData.length) {
            vertexData = Arrays.copyOf(
                    vertexData,
                    Math.max(
                            newSize,
                            vertexData.length << 1
                    )
            );
        }
    }

    @Override
    public VertexBuilder useAtlas(Id id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public VertexBuilder useTexture(CpuTexture texture) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public VertexBuilder useBlockId(int blockId) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public VertexBuilder addVertex(float x, float y, float z) {
        int index = size;
        size = index + 6;

        requireCapacity(size);

        vertexData[index] = Float.floatToRawIntBits(x);
        vertexData[index + 1] = Float.floatToRawIntBits(y);
        vertexData[index + 2] = Float.floatToRawIntBits(z);

        // Default tint
        vertexData[index + 3] = VoxelColor.WHITE;

        // Default uv
        vertexData[index + 4] = FP_ZERO;
        vertexData[index + 5] = FP_ZERO;

        return this;
    }

    @Override
    public VertexBuilder setTint(int argb) {
        vertexData[size - 4] = argb;
        return this;
    }

    @Override
    public VertexBuilder setUv(float u, float v) {
        vertexData[size - 3] = Float.floatToRawIntBits(u);
        vertexData[size - 2] = Float.floatToRawIntBits(v);

        return this;
    }
}
