package at.redi2go.photonics.core.rendering.world;

import at.redi2go.photonics.api.mc.core.IBlockPos;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3fc;
import org.joml.Vector3ic;

public class WorldOrigin extends Vector3d {
    public WorldOrigin() {
        super();
    }

    public WorldOrigin(double x, double y, double z) {
        super(x, y, z);
    }

    public Vector3d applyOffset(IBlockPos pos) {
        return new Vector3d(
                (double) pos.x() - x,
                (double) pos.y() - y,
                (double) pos.z() - z
        );
    }

    public Vector3d applyOffset(Vector3ic pos) {
        return new Vector3d(
                (double) pos.x() - x,
                (double) pos.y() - y,
                (double) pos.z() - z
        );
    }


    public Vector3d applyOffset(Vector3fc pos) {
        return new Vector3d(
                (double) pos.x() - x,
                (double) pos.y() - y,
                (double) pos.z() - z
        );
    }

    public Vector3d applyOffset(Vector3dc pos) {
        return pos.sub(this, new Vector3d());
    }
}
