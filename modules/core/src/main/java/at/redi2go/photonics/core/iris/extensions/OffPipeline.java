package at.redi2go.photonics.core.iris.extensions;

import at.redi2go.photonics.api.shaders.PhotonicsProperties;
import at.redi2go.photonics.core.iris.AbstractPhotonicsExtension;
import at.redi2go.photonics.core.rendering.lights.HandheldItemSupplier;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;

public class OffPipeline extends AbstractPhotonicsExtension {
    public OffPipeline(
            PhotonicsProperties properties,
            AtlasDownloader atlasDownloader,
            HandheldItemSupplier handheldItemSupplier
    ) {
        super(properties, atlasDownloader, handheldItemSupplier);
    }

    @Override
    public void onRender() {

    }
}
