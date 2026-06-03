package at.redi2go.photonics.core.rendering.world.bakery.texture;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.mc.Id;

public interface AtlasDownloader extends Disposable {
    void preloadTexture(Id atlasId);

    AtlasTexture get(Id atlasId);
}
