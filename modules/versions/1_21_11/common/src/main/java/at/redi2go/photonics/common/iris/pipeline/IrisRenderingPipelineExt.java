package at.redi2go.photonics.common.iris.pipeline;

import at.redi2go.photonics.common.iris.pipeline.renderer.composite.PhCompositeRenderer;

import java.util.List;

public interface IrisRenderingPipelineExt {
    List<PhCompositeRenderer> getRenderers();
}
