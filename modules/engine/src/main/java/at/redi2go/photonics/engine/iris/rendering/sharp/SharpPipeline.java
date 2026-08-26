package at.redi2go.photonics.engine.iris.rendering.sharp;

import at.redi2go.photonics.engine.iris.pipeline.IrisPipeline;
import at.redi2go.photonics.engine.iris.properties.PhotonicsProperties;
import at.redi2go.photonics.engine.iris.rendering.PhotonicsPipeline;
import at.redi2go.photonics.engine.rendering.world.bakery.texture.AtlasDownloader;

public class SharpPipeline extends PhotonicsPipeline {
    public SharpPipeline(
            PhotonicsProperties phProperties,
            SharpProperties sharpProperties,
            AtlasDownloader atlasDownloader,
            IrisPipeline irisPipeline
    ) {
        super(phProperties, atlasDownloader, irisPipeline);
    }
}
