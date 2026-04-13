package at.redi2go.photonics.core.rendering.world.bakery.vertex;

import org.joml.Vector3f;

public class Vertex extends Vector3f {
    private int tint;
    private float u, v;

    public int tint() {
        return tint;
    }

    public float u() {
        return u;
    }

    public float v() {
        return v;
    }

    public void readVertex(VertexBuilderImpl builder) {
        x = builder.readFloat();
        y = builder.readFloat();
        z = builder.readFloat();

        tint = builder.readInt();

        u = builder.readFloat();
        v = builder.readFloat();
    }
}