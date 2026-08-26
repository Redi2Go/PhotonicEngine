package at.redi2go.photonics.common.iris.pipeline.buffers;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import org.jspecify.annotations.Nullable;

public class IrisBuffers {
    public static IntSet getUsedBuffers() {
        return IntSet.of();
    }

    public static void bindBuffers(@Nullable WorldRenderingPipeline pipeline, int programId) {
        if (pipeline instanceof IrisRenderingPipeline ext)
            ((InternalIrisBufferHolder) ext).bind(programId, getUsedBuffers());
    }

    public static void bindBuffers(int programId) {
        bindBuffers(
                Iris.getPipelineManager().getPipelineNullable(),
                programId
        );
    }
}
