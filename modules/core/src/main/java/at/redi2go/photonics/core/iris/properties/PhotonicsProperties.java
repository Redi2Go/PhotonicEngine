package at.redi2go.photonics.core.iris.properties;

import at.redi2go.photonics.core.TransparencyMode;
import at.redi2go.photonics.core.iris.properties.annotations.DefaultValue;
import at.redi2go.photonics.core.iris.properties.annotations.Defines;
import at.redi2go.photonics.core.iris.properties.annotations.FloatRange;
import at.redi2go.photonics.core.iris.properties.annotations.Key;
import at.redi2go.photonics.core.iris.properties.components.LightListProperties;
import at.redi2go.photonics.core.iris.properties.rendering.BlockLightProperties;
import at.redi2go.photonics.core.iris.properties.rendering.GiProperties;
import at.redi2go.photonics.core.iris.properties.rendering.HandheldProperties;
import at.redi2go.photonics.core.iris.rendering.PhotonicsRenderer;

public interface PhotonicsProperties {
    @DefaultValue("false")
    boolean isEnabled();

    @DefaultValue("OFF")
    @Key(legacy = "photonics.lightingMode")
    PhotonicsRenderer getRenderer();

    @DefaultValue("1.0")
    @FloatRange(min = 0.0f)
    @Defines("PH_RENDER_SCALE")
    float getRenderScale();

    @DefaultValue("NONE")
    @Key(legacy = "photonics.alphaMode")
    TransparencyMode getTransparencyMode();

    LightListProperties getLightListProperties();

    BlockLightProperties getBlockLightProperties();

    GiProperties getGiProperties();

    HandheldProperties getHandheldProperties();
}
