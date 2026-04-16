package at.redi2go.photonics.common.iris.pipeline;

import at.redi2go.photonics.core.iris.PhotonicsExtension;

import java.util.Optional;

public interface PipelineManagerExt {
    Optional<PhotonicsExtension> photonics();
}
