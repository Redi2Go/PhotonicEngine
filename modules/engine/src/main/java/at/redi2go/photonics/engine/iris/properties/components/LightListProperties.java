package at.redi2go.photonics.engine.iris.properties.components;

import at.redi2go.photonics.engine.iris.properties.annotations.DefaultValue;
import at.redi2go.photonics.engine.iris.properties.annotations.Defines;
import at.redi2go.photonics.engine.iris.properties.annotations.Key;

public interface LightListProperties {
    @DefaultValue("false")
    boolean isEnabled();

    @DefaultValue("1000")
    @Defines("PH_MAX_LIGHTS")
    @Key(legacy = "photonics.maxLights")
    int getSize();
}
