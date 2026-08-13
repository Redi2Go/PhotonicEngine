package at.redi2go.photonics.core.iris.rendering.off;

import at.redi2go.photonics.core.iris.properties.PhotonicsProperties;
import at.redi2go.photonics.core.iris.properties.PropertyDefines;

public interface OffProperties extends PropertyDefines {
    @Override
    default void defineProperties(PhotonicsProperties properties) {
        stringDefine("PH_OFF_ACTIVE", "");
    }
}
