package at.redi2go.photonics.core.iris.properties.rendering;

import at.redi2go.photonics.core.iris.properties.annotations.DefaultValue;
import at.redi2go.photonics.core.iris.properties.annotations.Defines;
import at.redi2go.photonics.core.iris.properties.annotations.IntRange;
import at.redi2go.photonics.core.iris.properties.annotations.Key;

public interface GiProperties {
    @DefaultValue("true")
    @Defines("PH_ENABLE_GI")
    @Key(legacy = "photonics.enableGi")
    boolean isEnabled();

    @DefaultValue("1")
    @IntRange(min = 0, max = 8)
    @Defines("PH_MAX_GI_BOUNCES")
    @Key(legacy = "photonics.maxGiBounces")
    int getMaxBounces();

    @IntRange(min = 1)
    @DefaultValue("100")
    @Defines("PH_MAX_GI_BOUNCES")
    int getMaxTraceSteps();

    @DefaultValue("false")
    @Defines("PH_ENABLE_BLOCKLIGHT_GI")
    @Key(legacy = "photonics.enableBlockLightGi")
    boolean includeBlockLight();
}
