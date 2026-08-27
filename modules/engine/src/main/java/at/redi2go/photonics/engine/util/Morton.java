package at.redi2go.photonics.engine.util;

import org.joml.Vector3i;
import org.joml.Vector3ic;

public class Morton {

    private static int expandBits(int value) {
        int v = value & 0x1F;

        v = (v | (v << 16)) & 0x030000FF;
        v = (v | (v << 8)) & 0x0300F00F;
        v = (v | (v << 4)) & 0x030C30C3;
        v = (v | (v << 2)) & 0x09249249;

        return v;
    }

    private static int shrinkBits(int value) {
        int v = value & 0x09249249;

        v = (v & 0x030C30C3) | ((v >> 2) & 0x030C30C3);
        v = (v & 0x0300F00F) | ((v >> 4) & 0x0300F00F);
        v = (v & 0x030000FF) | ((v >> 8) & 0x030000FF);
        v = (v & 15) | ((v >> 16) & 15);

        return v;
    }

    static int encodePos(int x, int y, int z)  {
        return expandBits(x) | (expandBits(y) << 1) | (expandBits(z) << 2);
    }

    private static Vector3ic[] decodeCache;

    static {
        // Only expected to be used for 4x4x4 cubes, so cache those values
        decodeCache = new Vector3ic[4 * 4 * 4];

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 4; z++) {
                    decodeCache[encodePos(x, y, z)] = new Vector3i(x, y, z);
                }
            }
        }
    }

    static Vector3ic decodePos(int index) {
        if (index < decodeCache.length) {
            return decodeCache[index];
        }

        return new Vector3i(
                shrinkBits(index),
                shrinkBits(index >> 1),
                shrinkBits(index >> 2)
        );
    }

    private Morton() {

    }
}
