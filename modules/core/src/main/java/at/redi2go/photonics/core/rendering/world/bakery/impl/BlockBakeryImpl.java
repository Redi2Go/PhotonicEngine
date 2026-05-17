package at.redi2go.photonics.core.rendering.world.bakery.impl;

import at.redi2go.photonics.api.mc.Id;
import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlockAndTintGetter;
import at.redi2go.photonics.api.mc.world.level.IBlockState;
import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.rendering.world.bakery.BaryPos;
import at.redi2go.photonics.core.rendering.world.bakery.BlockBakery;
import at.redi2go.photonics.core.rendering.world.bakery.BlockBuilder;
import at.redi2go.photonics.core.rendering.world.bakery.BlockMeshState;
import at.redi2go.photonics.core.rendering.world.bakery.BlockMesher;
import at.redi2go.photonics.core.rendering.world.bakery.Vertex;
import at.redi2go.photonics.core.rendering.world.bakery.VoxelConsumer;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;
import at.redi2go.photonics.core.rendering.world.bakery.texture.CpuTexture;
import at.redi2go.photonics.core.rendering.world.block.TextureData;
import at.redi2go.photonics.core.rendering.world.block.VoxelColor;
import at.redi2go.photonics.core.rendering.world.block.VoxelNormal;
import at.redi2go.photonics.core.rendering.world.block.palette.TintBuilder;
import org.jetbrains.annotations.Nullable;
import org.joml.RoundingMode;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockBakeryImpl implements BlockBakery {
    private static final Queue<int[]> MESH_ARRAYS = new ConcurrentLinkedQueue<>();
    private static final int INITIAL_MESH_ARRAY_SIZE = 1024;

    private final AtlasDownloader atlasDownloader;

    public BlockBakeryImpl(AtlasDownloader atlasDownloader) {
        this.atlasDownloader = atlasDownloader;
    }

    private static int[] pollMeshArray() {
        int[] result = MESH_ARRAYS.poll();
        if (result != null) return result;

        return new int[INITIAL_MESH_ARRAY_SIZE];
    }

    @Override
    public @Nullable <T extends BlockMeshState> MeshResult meshBlock(
            BlockMesher<T> mesher,
            T meshState,
            Vector3i blockChunkOffset,
            IBlockPos pos,
            IBlockState blockState,
            IBlockAndTintGetter blockAndTintGetter
    ) {
        var builder = new MeshResultImpl(pollMeshArray());

        mesher.meshBlock(
                meshState,
                blockChunkOffset,
                pos,
                blockState,
                blockAndTintGetter,
                builder
        );

        if (builder.vertexCount == 0) {
            builder.close();
            return null;
        } else return builder;
    }

    public class MeshResultImpl implements BlockBuilder, MeshResult {
        private int[] meshData;

        private int index = 0;
        private int size = 0;

        private float offsetX, offsetY, offsetZ = 0f;

        private int currentBlockId = -1;
        private CpuTexture currentTexture = null;
        private long currentTextureHash = 0;

        private int vertexCount = 0;
        private long vertexHash = 0;
        private int vertexIndex = -1;

        private boolean open = true;

        private final ArrayDeque<StateChange> stateChanges = new ArrayDeque<>();

        public MeshResultImpl(int[] meshData) {
            this.meshData = meshData;
        }

        @Override
        public long vertexHash() {
            return vertexHash;
        }

        // Buffer helpers

        public boolean hasNext() {
            return index < size;
        }

        public int read(int n) {
            var index = this.index;
            var end = index + n;
            if (end > size) throw new ArrayIndexOutOfBoundsException(end + " out of bounds for " + size);

            this.index = end;
            return index;
        }

        public int intAt(int index) {
            return meshData[index];
        }

        private void putInt(int index, int value) {
            meshData[index] = value;
        }

        public float floatAt(int index) {
            return Float.intBitsToFloat(meshData[index]);
        }

        private void putFloat(int index, float value) {
            meshData[index] = Float.floatToRawIntBits(value);
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

        // Meshing

        private void requireCapacity(int newSize) {
            if (newSize >= meshData.length) {
                meshData = Arrays.copyOf(
                        meshData,
                        Math.max(
                                newSize,
                                meshData.length << 2
                        )
                );
            }
        }

        @Override
        public BlockBuilder useAtlas(Id id) {
            return useTexture(atlasDownloader.get(id));
        }

        @Override
        public BlockBuilder useTexture(CpuTexture texture) {
            if (currentTexture == texture) return this;

            currentTexture = texture;
            currentTextureHash = texture.hashCode();
            submitState(new TextureChange(texture));

            return this;
        }

        @Override
        public BlockBuilder useBlockId(int blockId) {
            if (currentBlockId == blockId) return this;

            currentBlockId = blockId;
            submitState(new BlockIdChange(blockId));

            return this;
        }

        @Override
        public BlockBuilder useOffset(float x, float y, float z) {
            offsetX = x;
            offsetY = y;
            offsetZ = z;

            return this;
        }

        private void hashLastVertex() {
            int index = vertexIndex;
            if (index == -1) return;

            long hash = vertexHash;

            hash = hash * 31 + currentTextureHash;

            hash = hash * 31 + intAt(index);
            hash = hash * 31 + intAt(index + 1);
            hash = hash * 31 + intAt(index + 2);

            hash = hash * 31 + intAt(index + 4);
            hash = hash * 31 + intAt(index + 5);

            vertexHash = hash;
        }

        private static final int FP_ZERO = Float.floatToRawIntBits(0);

        @Override
        public BlockBuilder addVertex(float x, float y, float z) {
            hashLastVertex();

            int index = size;
            size = index + 6;
            requireCapacity(size);

            vertexIndex = index;
            vertexCount++;

            x += offsetX;
            y += offsetY;
            z += offsetZ;

            putFloat(index, x);
            putFloat(index + 1, y);
            putFloat(index + 2, z);

            // Default tint
            putInt(index + 3, VoxelColor.WHITE);

            // Default uv
            putInt(index + 4, FP_ZERO);
            putInt(index + 5, FP_ZERO);

            return this;
        }

        @Override
        public BlockBuilder setTint(int argb) {
            putInt(size - 3, argb);
            return this;
        }

        @Override
        public BlockBuilder setUv(float u, float v) {
            putFloat(size - 2, u);
            putFloat(size - 1, v);

            return this;
        }


        // Baking


        @Override
        public TintBuilder.Result tintData() {
            TintBuilder tintBuilder = new TintBuilder();

            int index = 3;
            int end = vertexCount * 6;

            while (index < end) {
                tintBuilder.add(intAt(index));
                index += 6;
            }

            return tintBuilder.build();
        }

        @Override
        public void bake(VoxelConsumer voxelConsumer) throws InterruptedException {
            RasterState rasterState = new RasterState();
            Vertex[] tri = new Vertex[3];

            while (hasNext()) {
                checkInterrupted();
                bakeQuad(rasterState, tri, voxelConsumer);
            }
        }

        private void bakeQuad(
                RasterState rasterState,
                Vertex[] tri,
                VoxelConsumer consumer
        ) throws InterruptedException {
            pollState();

            rasterState.readQuad(this);

            int tint = rasterState.v0().tint();
            CpuTexture texture = currentTexture;

            checkInterrupted();
            tri[0] = rasterState.v0();
            tri[1] = rasterState.v1();
            tri[2] = rasterState.v2();
            voxelizeTri(rasterState, tint, tri, texture, consumer);

            checkInterrupted();
            tri[0] = rasterState.v2();
            tri[1] = rasterState.v3();
            tri[2] = rasterState.v0();
            voxelizeTri(rasterState, tint, tri, texture, consumer);
        }

        private static final float BLOCK_SIZE_INV = 1f / 16f;
        private static final Vector3i VEC3I_ZERO = new Vector3i(0);
        private static final Vector3i VEC3I_ONE = new Vector3i(1);

        private void voxelizeTri(
                RasterState rasterState,
                int tint,
                Vertex[] tri,
                CpuTexture texture,
                VoxelConsumer consumer
        ) throws InterruptedException {
            Vector3f ba = tri[1].sub(tri[0], rasterState.ba());
            Vector3f ca = tri[2].sub(tri[0], rasterState.ca());

            Vector3f n = ba.cross(ca, rasterState.n());
            Vector3f normal = n.normalize(rasterState.normal());
            Vector3f normalHalf = normal.mul(0.5f, rasterState.normalHalf());

            Vector3i temp = rasterState.temp();
            Vector3i min = rasterState.min().set(Integer.MAX_VALUE);
            Vector3i max = rasterState.max().set(0);

            Vector3f vertex = rasterState.vertex();
            for (int i = 2; i >= 0; i--) {
                tri[i].mul(16, vertex).sub(normalHalf);

                min.min(temp.set(vertex, RoundingMode.FLOOR));
                max.max(temp.set(vertex, RoundingMode.CEILING));
            }

            max.max(min);
            max.sub(min).max(VEC3I_ONE);

            Vector3f voxelPos = rasterState.voxelPos();
            Vector3f worldPos = rasterState.worldPos();

            //TODO: Do multiple faces
            // Will work for straight blocks for now
            int normalIndex = VoxelNormal.getIndex(normal);

            for (int px = 0; px < max.x; px++) {
                for (int py = 0; py < max.y; py++) {
                    for (int pz = 0; pz < max.z; pz++) {
                        checkInterrupted();

                        int x = min.x + px;
                        int y = min.y + py;
                        int z = min.z + pz;

                        voxelPos.set(x + 0.5f, y + 0.5f, z + 0.5f);

                        voxelPos.sub(vertex, worldPos);
                        var dist = worldPos.dot(normal);

                        voxelPos.sub(normal.mul(dist, worldPos), worldPos);
                        temp.set(worldPos, RoundingMode.FLOOR);

                        if (temp.x != x || temp.y != y || temp.z != z) continue;

                        voxelPos.mul(BLOCK_SIZE_INV);
                        voxelPos.sub(tri[0]);

                        var baryPos = BaryPos.from(voxelPos, ba, ca, n);
                        var textureData = sample(texture, currentBlockId, tri, baryPos);
                        if (textureData == null) continue;

                        if (VoxelColor.a(textureData.color()) != 0)
                            consumer.acceptVoxel(x, y, z, normalIndex, tint, textureData);
                    }
                }
            }
        }

        private void checkInterrupted() throws InterruptedException {
            if (Thread.interrupted())
                throw new InterruptedException();
        }

        @Override
        public void close() {
            if (open) {
                MESH_ARRAYS.add(meshData);
                open = false;
            }
        }

        private static abstract class StateChange {
            int index = 0;

            public abstract void apply(MeshResultImpl builder);
        }

        private static class BlockIdChange extends StateChange {
            private final int blockId;

            private BlockIdChange(int blockId) {
                this.blockId = blockId;
            }

            @Override
            public void apply(MeshResultImpl builder) {
                builder.currentBlockId = blockId;
            }
        }

        private static class TextureChange extends StateChange {
            private final CpuTexture texture;

            private TextureChange(CpuTexture texture) {
                this.texture = texture;
            }

            @Override
            public void apply(MeshResultImpl builder) {
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
}
