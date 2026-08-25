package at.redi2go.photonics.engine.iris.rendering.restir;

import at.redi2go.photonics.engine.iris.properties.annotations.DefaultValue;
import at.redi2go.photonics.engine.iris.properties.annotations.Defines;
import at.redi2go.photonics.engine.iris.properties.annotations.IntRange;
import at.redi2go.photonics.engine.iris.properties.annotations.Key;

public interface RestirDiProperties {
    @DefaultValue("true")
    @Defines("PH_RESTIR_SOFT_SHADOWS")
    @Key(legacy = "photonics.restirSoftShadows")
    boolean useSoftShadows();

    @DefaultValue("4")
    @IntRange(min = 1)
    @Defines("PH_RESTIR_INITIAL_SAMPLES")
    @Key(legacy = "photonics.restirInitialSamples")
    int getInitialCandidates();
}
