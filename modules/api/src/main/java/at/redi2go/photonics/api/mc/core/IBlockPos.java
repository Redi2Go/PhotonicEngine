package at.redi2go.photonics.api.mc.core;

import org.joml.Vector3fc;

public interface IBlockPos {
    int x();
    int y();
    int z();

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
