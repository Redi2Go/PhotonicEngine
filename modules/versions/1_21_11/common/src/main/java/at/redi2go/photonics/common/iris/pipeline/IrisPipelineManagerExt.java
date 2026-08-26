package at.redi2go.photonics.common.iris.pipeline;

import at.redi2go.photonics.common.iris.pipeline.renderer.DeferredIrisPassAction;

import java.util.List;

public interface IrisPipelineManagerExt {
    List<DeferredIrisPassAction> getRenderers();
}
