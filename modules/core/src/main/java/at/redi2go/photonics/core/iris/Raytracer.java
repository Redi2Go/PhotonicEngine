package at.redi2go.photonics.core.iris;

import at.redi2go.photonics.api.shaders.IShaderPack;
import at.redi2go.photonics.core.iris.patching.Patch;
import org.jetbrains.annotations.Nullable;

public class Raytracer extends PhotonicsExtension {
    public Raytracer(IShaderPack pack, @Nullable Patch patch) {
        super(pack, patch);
    }

    @Override
    public void close() throws Exception {

    }
}
