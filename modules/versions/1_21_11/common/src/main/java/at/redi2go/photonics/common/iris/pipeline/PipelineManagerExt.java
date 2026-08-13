package at.redi2go.photonics.common.iris.pipeline;

import at.redi2go.photonics.common.iris.pipeline.renderer.DeferredIrisRenderer;
import at.redi2go.photonics.core.iris.rendering.PhotonicsPipeline;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface PipelineManagerExt {
    List<DeferredIrisRenderer> getRenderers();

    void setRenderers(@Nullable List<at.redi2go.photonics.common.iris.pipeline.renderer.PhotonicsRenderer> renderers);
}
