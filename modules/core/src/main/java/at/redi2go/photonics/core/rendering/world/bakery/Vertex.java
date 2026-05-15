package at.redi2go.photonics.core.rendering.world.bakery;

import at.redi2go.photonics.core.rendering.world.bakery.impl.BlockBakeryImpl;
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

    public void readVertex(BlockBakeryImpl.MeshResultImpl mesh) {
        int index = mesh.read(6);

        x = mesh.floatAt(index);
        y = mesh.floatAt(index + 1);
        z = mesh.floatAt(index + 2);

        tint = mesh.intAt(index + 3);

        u = mesh.floatAt(index + 4);
        v = mesh.floatAt(index + 5);
    }
}