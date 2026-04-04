package at.redi2go.photonics.impl.shaders;

import at.redi2go.photonics.api.shaders.PhotonicsProperties;

public class PhotonicsPropertiesImpl implements PhotonicsProperties {
    public boolean enabled = PhotonicsProperties.DEFAULT_PHOTONICS_ENABLED;

    @Override
    public boolean isPhotonicsEnabled() {
        return enabled;
    }
}
