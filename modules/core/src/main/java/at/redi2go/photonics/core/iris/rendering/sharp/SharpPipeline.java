package at.redi2go.photonics.core.iris.rendering.sharp;

import at.redi2go.photonics.core.iris.pipeline.IrisPipeline;
import at.redi2go.photonics.core.iris.properties.PhotonicsProperties;
import at.redi2go.photonics.core.iris.rendering.PhotonicsPipeline;
import at.redi2go.photonics.core.iris.rendering.off.OffProperties;
import at.redi2go.photonics.core.rendering.lights.HandheldItemSupplier;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;

public class SharpPipeline extends PhotonicsPipeline {
    public SharpPipeline(
            PhotonicsProperties phProperties,
            SharpProperties sharpProperties,
            AtlasDownloader atlasDownloader,
            HandheldItemSupplier handheldItemSupplier,
            IrisPipeline irisPipeline
    ) {
        super(phProperties, atlasDownloader, irisPipeline);
    }

    @Override
    public void onRender() {

    }
}
