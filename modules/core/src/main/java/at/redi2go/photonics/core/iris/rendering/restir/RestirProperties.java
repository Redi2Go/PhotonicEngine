package at.redi2go.photonics.core.iris.rendering.restir;

import at.redi2go.photonics.core.iris.properties.annotations.DefaultValue;
import at.redi2go.photonics.core.iris.properties.annotations.Defines;
import at.redi2go.photonics.core.iris.properties.annotations.IntRange;
import at.redi2go.photonics.core.iris.properties.annotations.Key;

public interface RestirProperties {
    RestirDiProperties getDiProperties();

    RestirGiProperties getGiProperties();

    @DefaultValue("4")
    @IntRange(min = 0, max = 4)
    @Defines("PH_RESTIR_SPATIAL_REUSE_SAMPLES")
    @Key(legacy = "photonics.restirSpatialReuseSamples")
    int getSpatialReuseSamples();

    @DefaultValue("32")
    @IntRange(min = 1)
    @Defines("PH_RESTIR_SPATIAL_REUSE_RADIUS")
    @Key(legacy = "photonics.restirSpatialReuseRadius")
    float getSpatialReuseRadius();

    @DefaultValue("32")
    @IntRange(min = 1)
    @Defines("PH_RESTIR_ACCUMULATION_FRAMES")
    @Key(legacy = "photonics.restirAccumulationFrames")
    int getHistoryLength();

    @DefaultValue("5")
    @IntRange(min = 0)
    @Defines("PH_RESTIR_DENOISER_PASSES")
    @Key(legacy = "photonics.restirDenoiserPasses")
    int getDenoiserPasses();
}
