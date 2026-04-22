package at.redi2go.photonics.core.rendering.world.bakery.impl;

import at.redi2go.photonics.core.rendering.world.bakery.Vertex;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import org.joml.Vector3f;

public class BlockModel {
    int blockId = -1;
    int vertexCount = 0;
    long vertexHash;

    Vector3f blockPos = new Vector3f();

    int contained;

    public void readBlock(BlockBakeryImpl builder) {
        int index = builder.read(8);

        vertexCount = builder.intAt(index);
        blockId = builder.intAt(index + 1);
        vertexHash = builder.longAt(index + 2);

        blockPos.x = builder.intAt(index + 4);
        blockPos.y = builder.intAt(index + 5);
        blockPos.z = builder.intAt(index + 6);

        contained = builder.intAt(index + 7);
    }

    public long hashVertices(BlockBakeryImpl builder, Vertex vertex, IntArraySet tint) {
        long hash = 1;

        for (int i = 0; i < vertexCount; i++) {
            vertex.readVertex(builder);
            if ((i & 3) == 0) tint.add(vertex.tint());

            hash = hash * 31 + Float.hashCode(vertex.x);
            hash = hash * 31 + Float.hashCode(vertex.y);
            hash = hash * 31 + Float.hashCode(vertex.z);

            hash = hash * 31 + Float.hashCode(vertex.u());
            hash = hash * 31 + Float.hashCode(vertex.v());
        }

        return hash;
    }
}
