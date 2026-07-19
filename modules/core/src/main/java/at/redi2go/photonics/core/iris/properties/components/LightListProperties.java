package at.redi2go.photonics.core.iris.properties.components;

import at.redi2go.photonics.core.iris.properties.annotations.DefaultValue;
import at.redi2go.photonics.core.iris.properties.annotations.Defines;
import at.redi2go.photonics.core.iris.properties.annotations.Key;

public interface LightListProperties {
    @DefaultValue("false")
    boolean isEnabled();

    @DefaultValue("1000")
    @Defines("PH_MAX_LIGHTS")
    @Key(legacy = "photonics.maxLights")
    int getSize();
}
