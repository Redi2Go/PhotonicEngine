package at.redi2go.photonics.core.iris;

import at.redi2go.photonics.core.iris.patching.Patch;
import org.jetbrains.annotations.Nullable;

/**
 * A photonics extension that does the bare minimum for Photonics shader api.
 * (defines & patching always patched files, implemented in PhotonicsExtension)
 */
public class DisabledPhotonicsExtension extends PhotonicsExtension {
    public DisabledPhotonicsExtension(@Nullable Patch patch) {
        super(patch);
    }

    @Override
    public void close() throws Exception {

    }
}
