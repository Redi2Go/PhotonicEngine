package at.redi2go.photonics.core.rendering.world.bakery.impl;

import at.redi2go.photonics.core.rendering.world.bakery.Vertex;
import org.joml.Vector3f;
import org.joml.Vector3i;

public record RasterState(
        Vertex v0,
        Vertex v1,
        Vertex v2,
        Vertex v3,

        Vector3f ba,
        Vector3f ca,

        Vector3f n,
        Vector3f normal,
        Vector3f normalHalf,

        Vector3i temp,
        Vector3i min,
        Vector3i max,

        Vector3f vertex,

        Vector3f voxelPos,
        Vector3f worldPos
) {
    public RasterState() {
        this(
                new Vertex(),
                new Vertex(),
                new Vertex(),
                new Vertex(),
                new Vector3f(),
                new Vector3f(),
                new Vector3f(),
                new Vector3f(),
                new Vector3f(),
                new Vector3i(),
                new Vector3i(),
                new Vector3i(),
                new Vector3f(),
                new Vector3f(),
                new Vector3f()
        );
    }

    public void readQuad(BlockBakeryImpl.MeshResultImpl mesh) {
        v0.readVertex(mesh);
        v1.readVertex(mesh);
        v2.readVertex(mesh);
        v3.readVertex(mesh);
    }
}
