package at.redi2go.photonics.common.iris.pipeline;

import at.redi2go.photonics.common.iris.pipeline.renderer.DeferredIrisRenderer;
import at.redi2go.photonics.common.iris.pipeline.renderer.PhotonicsRenderer;
import at.redi2go.photonics.core.iris.PhotonicsExtension;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface PipelineManagerExt {
    Optional<PhotonicsExtension> photonics();

    List<DeferredIrisRenderer> getRenderers();

    void setRenderers(@Nullable List<PhotonicsRenderer> renderers);
}
