package at.redi2go.photonics.core.rendering.world.block;

import org.joml.Vector3f;
import org.joml.Vector3fc;

public class VoxelNormal {
    private static final Vector3fc[] INDEX_TO_NORMAL = new Vector3f[6];

    public static int getIndex(Vector3fc normal) {
        float x = normal.x();
        float y = normal.y();
        float z = normal.z();

        int index = (int) (Math.abs(x) * (x * 0.5 + 0.5)
                + Math.abs(y) * (y * 0.5 + 2.5)
                + Math.abs(z) * (z * 0.5 + 4.5)
                + 0.5);

        index = Math.max(index, 0);
        index = Math.min(index, 5);

        return index;
    }

    public static Vector3fc getNormal(int index) {
        return INDEX_TO_NORMAL[index];
    }

    private static void putNormal(float x, float y, float z) {
        Vector3f normal = new Vector3f(x, y, z);
        INDEX_TO_NORMAL[getIndex(normal)] = normal;
    }

    static {
        putNormal(0, -1, 0);
        putNormal(0, 1, 0);
        putNormal(0, 0, -1);
        putNormal(0, 0, 1);
        putNormal(-1, 0, 0);
        putNormal(1, 0, 0);
    }
}
