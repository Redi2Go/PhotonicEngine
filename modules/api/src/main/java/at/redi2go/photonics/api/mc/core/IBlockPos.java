package at.redi2go.photonics.api.mc.core;

import org.joml.Vector3fc;
import org.joml.Vector3i;

public interface IBlockPos {
    int x();
    int y();
    int z();

    IBlockPos offset(int x, int y, int z);

    default IBlockPos offset(Vector3i offset) {
        return offset(offset.x(), offset.y(), offset.z());
    }

    static IBlockPos zero() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IBlockPos of(int x, int y, int z) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IBlockPos of(Vector3fc vec3) {
        return of((int) vec3.x(), (int) vec3.y(), (int) vec3.z());
    }
}
