package at.redi2go.photonics.core.rendering.world.bakery.impl;

import at.redi2go.photonics.api.mc.Id;
import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlockAndTintGetter;
import at.redi2go.photonics.api.mc.world.level.IBlockState;
import at.redi2go.photonics.core.rendering.world.BlockRegistry;
import at.redi2go.photonics.core.rendering.world.WorldOrigin;
import at.redi2go.photonics.core.rendering.world.bakery.BaryPos;
import at.redi2go.photonics.core.rendering.world.bakery.BlockBakery;
import at.redi2go.photonics.core.rendering.world.bakery.BlockBuilder;
import at.redi2go.photonics.core.rendering.world.bakery.BlockConsumer;
import at.redi2go.photonics.core.rendering.world.bakery.BlockMesher;
import at.redi2go.photonics.core.rendering.world.bakery.Vertex;
import at.redi2go.photonics.core.rendering.world.bakery.VoxelConsumer;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;
import at.redi2go.photonics.core.rendering.world.bakery.texture.CpuTexture;
import at.redi2go.photonics.core.rendering.world.block.ContainedBlockEntry;
import at.redi2go.photonics.core.rendering.world.block.VoxelColor;
import at.redi2go.photonics.core.rendering.world.block.VoxelNormal;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import org.jetbrains.annotations.Nullable;
import org.joml.RoundingMode;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayDeque;
import java.util.Arrays;

public class BlockBakeryImpl implements BlockBakery, BlockBuilder {
    private final AtlasDownloader atlasDownloader;
    private final BlockRegistry registry;

    private short region = 0;
    private final BlockModel blockModel = new BlockModel();

    private final Vertex v0 = new Vertex();
    private final Vertex v1 = new Vertex();
    private final Vertex v2 = new Vertex();
    private final Vertex v3 = new Vertex();

    private final Vertex[] tri = new Vertex[3];


    public BlockBakeryImpl(
            AtlasDownloader atlasDownloader,
            BlockRegistry registry
    ) {
        this.atlasDownloader = atlasDownloader;
        this.registry = registry;
    }

    @Override
    public void setRegion(short region) {
        this.region = region;
    }

    @Override
    public void submitBlock(
            WorldOrigin origin,
            IBlockPos pos,
            IBlockState blockState,
            IBlockAndTintGetter blockAndTintGetter
    ) {
        var result = BlockMesher.REGISTRY.get(blockState.block())
                .orElse(null);

        if (result == null) return;

        result.meshBlock(
                origin,
                pos,
                blockState,
                blockAndTintGetter,
                this
        );
    }

    // Raster variables

    private static final float BLOCK_SIZE_INV = 1f / 16f;

    private final Vector3f ba = new Vector3f();
    private final Vector3f ca = new Vector3f();

    private final Vector3f n = new Vector3f();
    private final Vector3f normal = new Vector3f();
    private final Vector3f normalHalf = new Vector3f();

    private final Vector3i temp = new Vector3i();
    private final Vector3i min = new Vector3i();
    private final Vector3i max = new Vector3i();

    private final Vector3f vertex = new Vector3f();
    private static final Vector3i VEC3I_ZERO = new Vector3i(0);
    private static final Vector3i VEC3I_ONE = new Vector3i(1);

    private final Vector3f voxelPos = new Vector3f();
    private final Vector3f worldPos = new Vector3f();

    private void voxelizeTri(
            CpuTexture texture,
            int blockId,
            int tint,
            Vertex[] tri,
            VoxelConsumer consumer
    ) {
        Vector3f ba = tri[1].sub(tri[0], this.ba);
        Vector3f ca = tri[2].sub(tri[0], this.ca);

        Vector3f n = ba.cross(ca, this.n);
        Vector3f normal = n.normalize(this.normal);
        Vector3f normalHalf = normal.mul(0.5f, this.normalHalf);

        Vector3i temp = this.temp;
        Vector3i min = this.min.set(Integer.MAX_VALUE);
        Vector3i max = this.max.set(0);

        Vector3f vertex = this.vertex;
        for (int i = 2; i >= 0; i--) {
            tri[i].mul(16, vertex).sub(normalHalf);

            min.min(temp.set(vertex, RoundingMode.FLOOR));
            max.max(temp.set(vertex, RoundingMode.CEILING));
        }

        min.max(VEC3I_ZERO);
        max.max(min);

        max.sub(min).max(VEC3I_ONE);

        Vector3f voxelPos = this.voxelPos;
        Vector3f worldPos = this.worldPos;

        //TODO: Do multiple faces
        // Will work for straight blocks for now
        int normalIndex = VoxelNormal.getIndex(normal);

        for (int px = 0; px < max.x; px++) {
            for (int py = 0; py < max.y; py++) {
                for (int pz = 0; pz < max.z; pz++) {
                    int x = min.x + px;
                    int y = min.y + py;
                    int z = min.z + pz;

                    voxelPos.set(x + 0.5f, y + 0.5f, z + 0.5f);

                    voxelPos.sub(vertex, worldPos);
                    var dist = worldPos.dot(normal);
                    if (dist >= 0.5f) continue;

                    voxelPos.sub(normal.mul(dist, worldPos), worldPos);

                    if ((int) worldPos.x != x
                            || (int) worldPos.y != y
                            || (int) worldPos.z != z
                    ) continue;

                    voxelPos.mul(BLOCK_SIZE_INV);
                    voxelPos.sub(tri[0]);

                    var baryPos = BaryPos.from(voxelPos, ba, ca, n);
                    var textureData = sample(texture, blockId, tri, baryPos);
                    if (textureData == null) continue;

                    if (VoxelColor.a(textureData.color()) != 0)
                        consumer.acceptVoxel(x, y, z, region, normalIndex, tint, textureData);
                }
            }
        }
    }

    private void bakeQuad(int blockId, VoxelConsumer consumer, @Nullable Vector3f blockOffset) {
        v0.readVertex(this);
        v1.readVertex(this);
        v2.readVertex(this);
        v3.readVertex(this);

        if (blockOffset != null) {
            v0.add(blockOffset);
            v1.add(blockOffset);
            v2.add(blockOffset);
            v3.add(blockOffset);
        }

        int tint = v0.tint();
        CpuTexture texture = currentTexture;

        tri[0] = v0;
        tri[1] = v1;
        tri[2] = v2;
        voxelizeTri(texture, blockId, tint, tri, consumer);

        tri[0] = v2;
        tri[1] = v3;
        tri[2] = v0;
        voxelizeTri(texture, blockId, tint, tri, consumer);
    }

    private void bakeContainedBlock(BlockConsumer consumer) {
        IntArraySet tint = new IntArraySet();

        int index = this.index;
        long vertexHash = blockModel.hashVertices(this, v0, tint);

        int blockId = blockModel.blockId;
        Vector3f blockPos = blockModel.blockPos;

        int blockPosX = (int) blockPos.x << 4;
        int blockPosY = (int) blockPos.y << 4;
        int blockPosZ = (int) blockPos.z << 4;

        var result = registry.newContainedEntry(vertexHash);
        if (!(result instanceof ContainedBlockEntry.Builder builder)) {
            var block = result.createVariant(tint, 0, region);
            if (block == null) return;

            consumer.acceptBlock(
                    blockPosX, blockPosY, blockPosZ,
                    region,
                    block
            );

            return;
        }

        int quadCount = blockModel.vertexCount >> 2;

        builder.setRegion(region);
        builder.setSkylight(0);

        this.index = index;
        for (int quad = 0; quad < quadCount; quad++)
            bakeQuad(blockId, builder, null);

        var block = builder.build();
        if (block == null) return;

        consumer.acceptBlock(
                blockPosX, blockPosY, blockPosZ,
                region,
                block
        );
    }

    private void bakeBlockModel(VoxelConsumer voxelConsumer) {
        int blockId = blockModel.blockId;
        Vector3f blockPos = blockModel.blockPos;
        int quadCount = blockModel.vertexCount >> 2;

        for (int quad = 0; quad < quadCount; quad++)
            bakeQuad(blockId, voxelConsumer, blockPos);
    }

    @Override
    public void bake(VoxelConsumer voxelConsumer, BlockConsumer blockConsumer) {
        while (hasNext()) {
            blockModel.readBlock(this);

            if (blockModel.isContained())
                bakeContainedBlock(blockConsumer);
            else
                bakeBlockModel(voxelConsumer);
        }
    }


    // Block builder impl

    private final ArrayDeque<StateChange> stateChanges = new ArrayDeque<>();

    private int index = 0;
    private int size = 0;

    private int[] meshData = new int[1024];
    private float offsetX, offsetY, offsetZ = 0f;

    private CpuTexture currentTexture = null;

    @Override
    public void reset() {
            index = 0;
            size = 0;
            offsetX = offsetY = offsetZ = 0f;
            currentTexture = null;
            stateChanges.clear();
    }

    // State handling

    private void submitState(StateChange change) {
        change.index = size;

        stateChanges.addLast(change);
    }

    private void pollState() {
        while (!stateChanges.isEmpty() && stateChanges.peekFirst().index <= index)
            stateChanges.pop().apply(this);
    }

    // Reading

    public boolean hasNext() {
        return index < size;
    }

    public int readInt() {
        if (index >= size)
            throw new ArrayIndexOutOfBoundsException(index + " out of bounds for " + size);

        pollState();

        return meshData[index++];
    }

    private void requireCapacity(int newSize) {
        if (newSize >= meshData.length) {
            meshData = Arrays.copyOf(
                    meshData,
                    Math.max(
                            newSize,
                            meshData.length << 1
                    )
            );
        }
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public BlockBuilder useAtlas(Id id) {
        return useTexture(atlasDownloader.get(id));
    }

    @Override
    public BlockBuilder useTexture(CpuTexture texture) {
        submitState(new TextureChange(texture));

        return this;
    }

    @Override
    public BlockBuilder useOffset(float x, float y, float z) {
       offsetX = x;
       offsetY = y;
       offsetZ = z;

       return this;
    }

    // Vertex building
    private static final int FP_ZERO = Float.floatToRawIntBits(0);
    private static final int FP_MAX_VALUE = Float.floatToRawIntBits(Integer.MAX_VALUE);

    private int blockIndex = 0;

    @Override
    public BlockBuilder beginBlock(int blockId, Vector3d blockVoxelPos) {
        int index = size;
        size = index + 11;

        requireCapacity(size);

        blockIndex = index;

        meshData[index] = 0; // Vertex count
        meshData[index + 1] = blockId;

        // Block pos

        meshData[index + 2] = (int) blockVoxelPos.x;
        meshData[index + 3] = (int) blockVoxelPos.y;
        meshData[index + 4] = (int) blockVoxelPos.z;

        // Min vertex

        meshData[index + 5] = FP_MAX_VALUE;
        meshData[index + 6] = FP_MAX_VALUE;
        meshData[index + 7] = FP_MAX_VALUE;

        // Max vertex

        meshData[index + 8] = FP_ZERO;
        meshData[index + 9] = FP_ZERO;
        meshData[index + 10] = FP_ZERO;

        return this;
    }

    private void minComponent(int index, float value) {
        meshData[index] = Float.floatToRawIntBits(
                Math.min(
                        Float.intBitsToFloat(meshData[index]),
                        value
                )
        );
    }

    private void maxComponent(int index, float value) {
        meshData[index] = Float.floatToRawIntBits(
                Math.max(
                        Float.intBitsToFloat(meshData[index]),
                        value
                )
        );
    }


    private void minBlockVertex(float x, float y, float z) {
        minComponent(blockIndex + 5, x);
        minComponent(blockIndex + 6, y);
        minComponent(blockIndex + 7, z);
    }

    private void maxBlockVertex(float x, float y, float z) {
        maxComponent(blockIndex + 8, x);
        maxComponent(blockIndex + 9, y);
        maxComponent(blockIndex + 10, z);
    }

    @Override
    public BlockBuilder addVertex(float x, float y, float z) {
        int index = size;
        size = index + 6;

        requireCapacity(size);
        x+= offsetX;
        y+= offsetY;
        z+= offsetZ;

        meshData[blockIndex]++;
        minBlockVertex(x, y, z);
        maxBlockVertex(x, y, z);

        meshData[index] = Float.floatToRawIntBits(x);
        meshData[index + 1] = Float.floatToRawIntBits(y);
        meshData[index + 2] = Float.floatToRawIntBits(z);

        // Default tint
        meshData[index + 3] = VoxelColor.WHITE;

        // Default uv
        meshData[index + 4] = FP_ZERO;
        meshData[index + 5] = FP_ZERO;

        return this;
    }

    @Override
    public BlockBuilder setTint(int argb) {
        meshData[size - 3] = argb;
        return this;
    }

    @Override
    public BlockBuilder setUv(float u, float v) {
        meshData[size - 2] = Float.floatToRawIntBits(u);
        meshData[size - 1 ] = Float.floatToRawIntBits(v);

        return this;
    }

    private static abstract class StateChange {
        int index = 0;

        public abstract void apply(BlockBakeryImpl builder);
    }

    private static class TextureChange extends StateChange {
        private final CpuTexture texture;

        private TextureChange(CpuTexture texture) {
            this.texture = texture;
        }

        @Override
        public void apply(BlockBakeryImpl builder) {
            builder.currentTexture = texture;
        }
    }



    private static int signBit(float value) {
        return Float.floatToRawIntBits(value) & Integer.MIN_VALUE;
    }

    private static TextureData sample(
            CpuTexture texture,
            int blockId,
            Vertex[] tri,
            BaryPos barycentricPos
    ) {
        float w1 = barycentricPos.x;
        float w2 = barycentricPos.y;
        float w3 = barycentricPos.z;
        if ((signBit(w1) | signBit(w2) | signBit(w3)) != 0) return null;

        float u = Math.fma(w1, tri[2].u(), Math.fma(w2, tri[1].u(), w3 * tri[0].u()));
        float v = Math.fma(w1, tri[2].v(), Math.fma(w2, tri[1].v(), w3 * tri[0].v()));

        return texture.sample(blockId, u, v);
    }
}
