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
        x = builder.readFloat();
        y = builder.readFloat();
        z = builder.readFloat();

        tint = builder.readInt();

        u = builder.readFloat();
        v = builder.readFloat();
    }
}