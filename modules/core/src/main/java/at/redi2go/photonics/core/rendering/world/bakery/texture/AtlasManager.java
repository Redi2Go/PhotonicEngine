package at.redi2go.photonics.core.rendering.world.bakery.texture;

import at.redi2go.photonics.api.mc.Id;

public interface AtlasManager {
    CpuTexture get(Id atlasId);
}
