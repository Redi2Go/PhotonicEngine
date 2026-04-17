package at.redi2go.photonics.core.rendering.world.bakery;

import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlockAndTintGetter;
import at.redi2go.photonics.api.mc.world.level.IBlockState;
import at.redi2go.photonics.core.rendering.world.WorldOrigin;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;
import at.redi2go.photonics.core.rendering.world.bakery.texture.CpuTexture;
import at.redi2go.photonics.core.rendering.world.bakery.vertex.Vertex;
import at.redi2go.photonics.core.rendering.world.bakery.vertex.VertexBuilderImpl;
import at.redi2go.photonics.core.rendering.world.block.VoxelColor;
import at.redi2go.photonics.core.rendering.world.block.VoxelNormal;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

public class BlockBakery {
    private static final float BLOCK_SIZE_INV = 1f / 16f;

    private final VertexBuilderImpl vertexBuilder;

    private final Vertex v0 = new Vertex();
    private final Vertex v1 = new Vertex();
    private final Vertex v2 = new Vertex();
    private final Vertex v3 = new Vertex();

    private final Vector4f tint = new Vector4f();

    private final Vertex[] tri = new Vertex[3];

    public BlockBakery(AtlasDownloader atlasDownloader) {
        vertexBuilder = new VertexBuilderImpl(atlasDownloader);
    }

    public void meshBlock(
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
                vertexBuilder
        );
    }

    public void bake(VoxelConsumer consumer) {
        while (vertexBuilder.hasNext()) {
            v0.readVertex(vertexBuilder);
            v1.readVertex(vertexBuilder);
            v2.readVertex(vertexBuilder);
            v3.readVertex(vertexBuilder);

            CpuTexture texture = vertexBuilder.currentTexture();
            int blockId = vertexBuilder.currentBlockId();

            VoxelColor.toVector(v0.tint(), tint);

            tri[0] = v0;
            tri[1] = v1;
            tri[2] = v2;
            voxelizeTri(texture, blockId, tint, tri, consumer);

            tri[0] = v2;
            tri[1] = v3;
            tri[2] = v0;
            voxelizeTri(texture, blockId, tint, tri, consumer);
        }
    }

    //TODO: tinting
    private void voxelizeTri(
            CpuTexture texture,
            int blockId,
            Vector4f tint,
            Vertex[] tri,
            VoxelConsumer consumer
    ) {
        Vector3f ba = tri[1].sub(tri[0], new Vector3f());
        Vector3f ca = tri[2].sub(tri[0], new Vector3f());

        Vector3f n = ba.cross(ca, new Vector3f());
        Vector3f normal = n.normalize(new Vector3f());

        Vector3i temp = new Vector3i();
        Vector3i min = new Vector3i(Integer.MAX_VALUE);
        Vector3i max = new Vector3i(0);

        Vector3f vertex = new Vector3f();
        for (int i = 2; i >= 0; i--) {
            tri[i].mul(16, vertex);

            min.min(temp.set(vertex, RoundingMode.FLOOR));
            max.max(temp.set(vertex, RoundingMode.CEILING));
        }

        min.max(new Vector3i(0));
        max.max(min);

        Vector3f voxelPos = new Vector3f();
        Vector3f worldPos = new Vector3f();

        //TODO: Do multiple faces
        // Will work for straight blocks for now
        int normalIndex = VoxelNormal.getIndex(normal);

        for (int x = min.x; x <= max.x; x++) {
            for (int y = min.y; y <= max.y; y++) {
                for (int z = min.z; z <= max.z; z++) {
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

                    consumer.accept(x, y, z, normalIndex, textureData.withTint(tint));
                }
            }
        }
    }

    private static TextureData sample(CpuTexture texture, int blockId, Vertex[] tri, BaryPos barycentricPos) {
        float w1 = barycentricPos.x;
        float w2 = barycentricPos.y;
        float w3 = barycentricPos.z;
        if (w1 < 0 || w2 < 0 || w3 < 0) return null;

        float u = Math.fma(w1, tri[2].u(), Math.fma(w2, tri[1].u(), w3 * tri[0].u()));
        float v = Math.fma(w1, tri[2].v(), Math.fma(w2, tri[1].v(), w3 * tri[0].v()));

        return texture.sample(blockId, u, v);
    }
}
