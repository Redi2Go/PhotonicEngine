package at.redi2go.photonics.core.rendering.world.bakery;

import at.redi2go.photonics.core.rendering.world.bakery.vertex.Vertex;
import org.joml.Vector3f;

/**
 * Barycentric Position, only exists for readability reasons
 */
public class BaryPos extends Vector3f {
    public BaryPos() {
        super();
    }

    public BaryPos(float x, float y, float z) {
        super(x, y, z);
    }

    public static BaryPos from(Vector3f pos, Vertex[] tri) {
        Vector3f u = tri[1].sub(tri[0], new Vector3f());
        Vector3f v = tri[2].sub(tri[0], new Vector3f());
        Vector3f n = u.cross(v, new Vector3f());

        return from(pos, u, v, n);
    }

    public static BaryPos from(
            Vector3f pos,
            Vector3f ba,
            Vector3f ca,
            Vector3f n
    ) {
        var nDotN = n.dot(n);

        var result = new BaryPos(
                ba.cross(pos, new Vector3f()).dot(n) / nDotN,
                pos.cross(ca, new Vector3f()).dot(n) / nDotN,
                0
        );

        result.z = 1 - result.x - result.y;

        return result;
    }
}
