package at.redi2go.photonics.core.rendering.lights;

import at.redi2go.photonics.core.config.lights.BlockLightInfo;
import org.joml.Vector3i;

public class LightInvalidation {
    public final Vector3i pos;
    public BlockLightInfo before;
    public BlockLightInfo after;

    public int beforeIndex = -1;
    public int afterIndex = -1;

    public LightInvalidation(Vector3i pos) {
        this.pos = pos;
    }
}
