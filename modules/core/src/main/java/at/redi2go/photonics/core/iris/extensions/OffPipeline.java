package at.redi2go.photonics.core.iris.extensions;

import at.redi2go.photonics.api.shaders.PhotonicsProperties;
import at.redi2go.photonics.core.iris.AbstractPhotonicsExtension;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;

public class OffPipeline extends AbstractPhotonicsExtension {
    public OffPipeline(PhotonicsProperties properties, AtlasDownloader atlasDownloader) {
        super(properties, atlasDownloader);
    }

    @Override
    public void onRender() {

    }
}
