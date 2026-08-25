package at.redi2go.photonics.engine.iris.rendering.restir;

import at.redi2go.photonics.engine.iris.properties.annotations.DefaultValue;
import at.redi2go.photonics.engine.iris.properties.annotations.Defines;
import at.redi2go.photonics.engine.iris.properties.annotations.Key;

public interface RestirGiProperties {
    @DefaultValue("false")
    @Defines("PH_RESTIR_COMBINED_GI")
    @Key(legacy = "photonics.restirCombinedGi")
    boolean isEnabled();
}
