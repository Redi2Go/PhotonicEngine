package at.redi2go.photonics.engine.iris.rendering.off;

import at.redi2go.photonics.engine.iris.properties.PhotonicsProperties;
import at.redi2go.photonics.engine.iris.properties.PropertyDefines;

public interface OffProperties extends PropertyDefines {
    @Override
    default void defineProperties(PhotonicsProperties properties) {
        stringDefine("PH_OFF_ACTIVE", "");
    }
}
