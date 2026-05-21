package at.redi2go.photonics.core.iris.extensions;

import at.redi2go.photonics.api.shaders.PhotonicsProperties;
import at.redi2go.photonics.core.iris.AbstractPhotonicsExtension;
import at.redi2go.photonics.core.iris.pipeline.rendering.IrisPipelineFactory;
import at.redi2go.photonics.core.rendering.lights.HandheldItemSupplier;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;

public class BasicPipeline extends AbstractPhotonicsExtension {
    public BasicPipeline(
            PhotonicsProperties properties,
            AtlasDownloader atlasDownloader,
            HandheldItemSupplier handheldItemSupplier,
            IrisPipelineFactory pipeline
    ) {
        super(properties, atlasDownloader, handheldItemSupplier);
    }

    @Override
    public void onRender() {

    }
}
