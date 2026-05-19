package at.redi2go.photonics.core.rendering.world.allocator;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.config.lights.BlockLightInfo;

public interface WorldLightMemory extends Disposable {
    int entryData();

    void setLight(BlockLightInfo light, int blockId);

    void upload();
}
