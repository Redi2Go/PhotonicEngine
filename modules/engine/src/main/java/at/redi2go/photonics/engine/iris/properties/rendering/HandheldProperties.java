package at.redi2go.photonics.engine.iris.properties.rendering;

import at.redi2go.photonics.engine.iris.properties.annotations.DefaultValue;
import at.redi2go.photonics.engine.iris.properties.annotations.Defines;
import at.redi2go.photonics.engine.iris.properties.annotations.FloatRange;
import at.redi2go.photonics.engine.iris.properties.annotations.Key;

public interface HandheldProperties {
    @DefaultValue("true")
    @Defines("PH_ENABLE_HANDHELD_LIGHT")
    @Key(legacy = "photonics.enableHandheldLight")
    boolean isEnabled();

    @FloatRange(min = 0.0f)
    @DefaultValue("0.2")
    @Key(legacy = "photonics.enchantmentGlintStrength")
    float getGlintStrength();

    @DefaultValue("false")
    @Defines("PH_SEPARATE_HANDHELD_RAYS")
    @Key(legacy = "photonics.useSeparateHandheldRays")
    boolean getPerHandShadows();
}
