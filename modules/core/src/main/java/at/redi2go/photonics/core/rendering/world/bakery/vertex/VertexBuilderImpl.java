package at.redi2go.photonics.core.rendering.world.bakery.vertex;

import at.redi2go.photonics.api.mc.Id;
import at.redi2go.photonics.core.rendering.world.bakery.VertexBuilder;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;
import at.redi2go.photonics.core.rendering.world.bakery.texture.CpuTexture;
import at.redi2go.photonics.core.rendering.world.block.VoxelColor;

import java.util.ArrayDeque;
import java.util.Arrays;

public class VertexBuilderImpl implements VertexBuilder {
    private static final int FP_ZERO = Float.floatToRawIntBits(0);

    private final AtlasDownloader atlasDownloader;

    private int[] vertexData = new int[1024];
    private final ArrayDeque<StateChange> stateChanges = new ArrayDeque<>();

    private int index = 0;
    private int size = 0;

    private CpuTexture currentTexture = null;
    private int currentBlockId = Integer.MIN_VALUE;

    public VertexBuilderImpl(AtlasDownloader atlasDownloader) {
        this.atlasDownloader = atlasDownloader;
    }

    public void clear() {
        index = 0;
        size = 0;

        currentTexture = null;
        currentBlockId = Integer.MIN_VALUE;
        stateChanges.clear();
    }

    private void submitState(StateChange change) {
        change.index = size;

        stateChanges.addLast(change);
    }

    private void pollState() {
        while (!stateChanges.isEmpty() && stateChanges.peekFirst().index <= index)
            stateChanges.pop().apply(this);
    }

    public boolean hasNext() {
        return index < size;
    }

    public CpuTexture currentTexture() {
        return currentTexture;
    }

    public int currentBlockId() {
        return currentBlockId;
    }

    public int readInt() {
        if (index >= size)
            throw new ArrayIndexOutOfBoundsException(index + " out of bounds for " + size);

        pollState();

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
        return useTexture(atlasDownloader.get(id));
    }

    @Override
    public VertexBuilder useTexture(CpuTexture texture) {
        submitState(new TextureChange(texture));

        return this;
    }

    @Override
    public VertexBuilder useBlockId(int blockId) {
        submitState(new BlockIdChange(blockId));

        return this;
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

    private static abstract class StateChange {
        int index = 0;

        public abstract void apply(VertexBuilderImpl builder);
    }

    private static class TextureChange extends StateChange {
        private final CpuTexture texture;

        private TextureChange(CpuTexture texture) {
            this.texture = texture;
        }

        @Override
        public void apply(VertexBuilderImpl builder) {
            builder.currentTexture = texture;
        }
    }

    private static class BlockIdChange extends StateChange {
        private final int blockId;

        private BlockIdChange(int blockId) {
            this.blockId = blockId;
        }

        @Override
        public void apply(VertexBuilderImpl builder) {
            builder.currentBlockId = blockId;
        }
    }
}
