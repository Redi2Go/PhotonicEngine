package at.redi2go.photonics.core.iris.rendering.restir;

import at.redi2go.photonics.core.iris.properties.annotations.DefaultValue;
import at.redi2go.photonics.core.iris.properties.annotations.Defines;
import at.redi2go.photonics.core.iris.properties.annotations.Key;

public interface RestirGiProperties {
    @DefaultValue("false")
    @Defines("PH_RESTIR_COMBINED_GI")
    @Key(legacy = "photonics.restirCombinedGi")
    boolean isEnabled();
}
