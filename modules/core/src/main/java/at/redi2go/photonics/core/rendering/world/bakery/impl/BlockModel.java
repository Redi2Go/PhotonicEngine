package at.redi2go.photonics.core.rendering.world.bakery.impl;

import at.redi2go.photonics.core.rendering.world.bakery.Vertex;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import org.joml.Vector3f;

public class BlockModel {
    int blockId = -1;
    int vertexCount = 0;

    Vector3f blockPos = new Vector3f();

    Vector3f minVertex = new Vector3f();
    Vector3f maxVertex = new Vector3f();

    public void readBlock(BlockBakeryImpl builder) {
        vertexCount = builder.readInt();
        blockId = builder.readInt();

        blockPos.x = builder.readInt();
        blockPos.y = builder.readInt();
        blockPos.z = builder.readInt();

        minVertex.x = builder.readFloat();
        minVertex.y = builder.readFloat();
        minVertex.z = builder.readFloat();

        maxVertex.x = builder.readFloat();
        maxVertex.y = builder.readFloat();
        maxVertex.z = builder.readFloat();
    }

    public boolean isContained() {
        return minVertex.x >= 0 &&
                minVertex.y >= 0 &&
                minVertex.z >= 0 &&
                maxVertex.x <= 1.0 &&
                maxVertex.y <= 1.0 &&
                maxVertex.z <= 1.0;
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
