package at.redi2go.photonics.core.rendering.world.bakery.impl;

import at.redi2go.photonics.core.rendering.world.bakery.Vertex;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import org.joml.Vector3i;

public class BlockModel {
    int blockId = -1;
    int lod = 0;
    int vertexCount = 0;
    long vertexHash;

    Vector3i blockOffset = new Vector3i();

    int contained;

    public void readBlock(BlockBakeryImpl builder) {
        int index = builder.read(8);

        vertexCount = builder.intAt(index);
        blockId = builder.intAt(index + 1);
        lod = builder.intAt(index + 2);
        vertexHash = builder.longAt(index + 3);

        blockOffset.x = builder.intAt(index + 5);
        blockOffset.y = builder.intAt(index + 6);
        blockOffset.z = builder.intAt(index + 7);

        contained = builder.intAt(index + 8);
    }
}
