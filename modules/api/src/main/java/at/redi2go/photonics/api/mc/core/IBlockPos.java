package at.redi2go.photonics.api.mc.core;

import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

public interface IBlockPos {
    int ph$x();
    int ph$y();
    int ph$z();

    IBlockPos ph$offset(int x, int y, int z);

    default IBlockPos ph$offset(Vector3ic offset) {
        return ph$offset(offset.x(), offset.y(), offset.z());
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

    static IBlockPos of(Vector3ic vec3) {
        return of(vec3.x(), vec3.y(), vec3.z());
    }
}
