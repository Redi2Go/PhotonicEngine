package at.redi2go.photonics.core.util;

import org.joml.Vector3ic;

public class VectorUtil {
    public static boolean isPowerOf2(Vector3ic vector) {
        return isPowerOf2(vector.x()) && isPowerOf2(vector.y()) && isPowerOf2(vector.z());
    }

    static boolean arePowerOf2Dimensions(int width, int height, int depth) {
        return isPowerOf2(width) && isPowerOf2(height) && isPowerOf2(depth);
    }

    private static boolean isPowerOf2(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }
}
