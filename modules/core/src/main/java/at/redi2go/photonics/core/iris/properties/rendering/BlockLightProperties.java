package at.redi2go.photonics.core.iris.properties.rendering;

import at.redi2go.photonics.core.iris.properties.annotations.DefaultValue;
import at.redi2go.photonics.core.iris.properties.annotations.Defines;
import at.redi2go.photonics.core.iris.properties.annotations.Key;

public interface BlockLightProperties {
    @DefaultValue("true")
    @Defines("PH_ENABLE_BLOCKLIGHT")
    @Key(legacy = "photonics.enableBlockLight")
    boolean isEnabled();
}
