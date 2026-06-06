package at.redi2go.photonics.core.config;

import at.redi2go.photonics.api.mc.world.level.IBlock;
import at.redi2go.photonics.core.config.lights.LightDefines;
import at.redi2go.photonics.core.config.lights.LightGroup;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The class that represents Photonics's config
 */
public class PhStorage {
    private static final LinkedHashMap<String, LightGroup> EMPTY_LIGHTS = new LinkedHashMap<>(0);

    public boolean multiThreadingEnabled = true;
    public LightDefines defines = LightDefines.EMPTY;
    public LinkedHashMap<String, LightGroup> lights = EMPTY_LIGHTS;

    /**
     * Overrides the values in {@link #lights}
     */
    public Map<IBlock, Boolean> raytracedLights = Map.of();
}
