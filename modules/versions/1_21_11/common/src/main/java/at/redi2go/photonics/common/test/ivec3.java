package at.redi2go.photonics.common.test;

import net.minecraft.core.BlockPos;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3ic;

public record ivec3(
        int x,
        int y,
        int z
) {
    public static final ivec3 ZERO = new ivec3(0);

    public ivec3(Vector3ic pos) {
        this((int) pos.x(), (int) pos.y(), (int) pos.z());
    }

    public ivec3(Vector3d pos) {
        this((int) pos.x, (int) pos.y, (int) pos.z);
    }

    public ivec3(Vector3f pos) {
        this((int) pos.x, (int) pos.y, (int) pos.z);
    }

    public ivec3(int value) {
        this(value, value, value);
    }

    public int get(int component) {
        return switch (component) {
            case 0 -> x;
            case 1 -> y;
            case 2 -> z;
            default -> throw new IllegalArgumentException();
        };
    }

    public ivec3 plus(ivec3 other) {
        return new ivec3(
                x + other.x,
                y + other.y,
                z + other.z
        );
    }

    public ivec3 minus(ivec3 other) {
        return new ivec3(
                x - other.x,
                y - other.y,
                z - other.z
        );
    }

    public ivec3 mul(ivec3 other) {
        return new ivec3(
                x * other.x,
                y * other.y,
                z * other.z
        );
    }

    public ivec3 div(ivec3 other) {
        return new ivec3(
                x / other.x,
                y / other.y,
                z / other.z
        );
    }

    public ivec3 mod(ivec3 other) {
        return new ivec3(
                x % other.x,
                y % other.y,
                z % other.z
        );
    }


    public ivec3 shr(ivec3 other) {
        return new ivec3(
                x >> other.x,
                y >> other.y,
                z >> other.z
        );
    }

    public ivec3 shr(int bit) {
       return new ivec3(
                x >> bit,
                y >> bit,
                z >> bit
        );
    }

    public ivec3 shl(ivec3 other) {
        return new ivec3(
                x << other.x,
                y << other.y,
                z << other.z
        );
    }

    public ivec3 shl(int bit) {
        return new ivec3(
                x << bit,
                y << bit,
                z << bit
        );
    }

    public ivec3 and(int value) {
        return new ivec3(
                x & value,
                y & value,
                z & value
        );
    }

    public ivec3 xor(ivec3 other) {
        return new ivec3(
                x ^ other.x,
                y ^ other.y,
                z ^ other.z
        );
    }

    public ivec3 max(int value) {
        return new ivec3(
                Math.max(x, value),
                Math.max(y, value),
                Math.max(z, value)
        );
    }

    public Vector3f toVec3() {
        return new Vector3f(x, y, z);
    }

    public BlockPos toBlockPos() {
        return new BlockPos(x, y, z);
    }

    public int flatten() {
        return flatten(x, y, z);
    }

    private static int expandBits(int value) {
        int v = value & 0x1F;
        v = (v | (v << 16)) & 0x030000FF;
        v = (v | (v <<  8)) & 0x0300F00F;
        v = (v | (v <<  4)) & 0x030C30C3;
        v = (v | (v <<  2)) & 0x09249249;
        return v;
    }

    public static int flatten(int x, int y, int z) {
        return expandBits(x) | (expandBits(y) << 1) | (expandBits(z) << 2);
    }
}
