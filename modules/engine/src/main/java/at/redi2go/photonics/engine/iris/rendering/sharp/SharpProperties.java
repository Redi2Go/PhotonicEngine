package at.redi2go.photonics.engine.iris.rendering.sharp;

import at.redi2go.photonics.engine.iris.properties.PhotonicsProperties;
import at.redi2go.photonics.engine.iris.properties.PropertyDefines;
import at.redi2go.photonics.engine.iris.properties.annotations.DefaultValue;
import at.redi2go.photonics.engine.iris.properties.annotations.Defines;
import at.redi2go.photonics.engine.iris.properties.annotations.IntRange;
import at.redi2go.photonics.engine.iris.properties.annotations.Key;

public interface SharpProperties extends PropertyDefines {
    @DefaultValue("20")
    @IntRange(min = 1)
    @Defines("PH_MAX_SAMPLES")
    @Key(legacy = "photonics.maxSamples")
    int getMaxSamples();

    @Override
    default void defineProperties(PhotonicsProperties properties) {
        stringDefine("PH_SHARP_ACTIVE", "");
    }
}
