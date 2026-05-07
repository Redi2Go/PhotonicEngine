package at.redi2go.photonics.common.iris.pipeline;

import at.redi2go.photonics.common.iris.buffers.GlBufferHolder;

public interface IrisRenderingPipelineExt {
    GlBufferHolder photonics$bufferHolder();

    void onSelect();
}
