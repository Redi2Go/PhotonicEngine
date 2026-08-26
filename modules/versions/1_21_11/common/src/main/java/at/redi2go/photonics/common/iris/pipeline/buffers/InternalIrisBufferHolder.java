package at.redi2go.photonics.common.iris.pipeline.buffers;

import at.redi2go.photonics.engine.iris.pipeline.buffers.IrisBufferHolder;
import it.unimi.dsi.fastutil.ints.IntSet;

public interface InternalIrisBufferHolder extends IrisBufferHolder {
    void bind(int shaderId, IntSet usedBuffers);
}
