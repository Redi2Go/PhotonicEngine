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

    public void readVertex(BlockBakeryImpl builder) {
        int index = builder.read(6);

        x = builder.floatAt(index);
        y = builder.floatAt(index + 1);
        z = builder.floatAt(index + 2);

        tint = builder.intAt(index + 3);

        u = builder.floatAt(index + 4);
        v = builder.floatAt(index + 5);
    }
}