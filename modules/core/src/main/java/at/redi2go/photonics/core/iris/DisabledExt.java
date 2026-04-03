package at.redi2go.photonics.core.iris;

import at.redi2go.photonics.api.shaders.IShaderPack;
import at.redi2go.photonics.core.iris.patching.Patch;
import org.jetbrains.annotations.Nullable;

/**
 * A photonics extension that does the bare minimum for Photonics shader api.
 * (defines & patching always patched files, implemented in PhotonicsExtension)
 */
class DisabledExt extends PhotonicsExtension {
    public DisabledExt(IShaderPack pack, @Nullable Patch patch) {
        super(pack, patch);
    }

    @Override
    public void close() throws Exception {

    }
}
