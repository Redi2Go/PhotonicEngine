package at.redi2go.photonics.engine.iris.rendering.off;

import at.redi2go.photonics.engine.iris.pipeline.IrisPipeline;
import at.redi2go.photonics.engine.iris.properties.PhotonicsProperties;
import at.redi2go.photonics.engine.iris.rendering.PhotonicsPipeline;
import at.redi2go.photonics.engine.rendering.lights.HandheldItemSupplier;
import at.redi2go.photonics.engine.rendering.world.bakery.texture.AtlasDownloader;

public class OffPipeline extends PhotonicsPipeline {
    public OffPipeline(
            PhotonicsProperties phProperties,
            OffProperties offProperties,
            AtlasDownloader atlasDownloader,
            IrisPipeline irisPipeline
    ) {
        super(phProperties, atlasDownloader, irisPipeline);
    }
}
