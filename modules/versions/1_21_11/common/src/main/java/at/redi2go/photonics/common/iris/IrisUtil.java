package at.redi2go.photonics.common.iris;

import at.redi2go.photonics.common.iris.pipeline.IrisRenderingPipelineExt;
import at.redi2go.photonics.common.iris.pipeline.PipelineManagerExt;
import at.redi2go.photonics.core.iris.PhotonicsExtension;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class IrisUtil {
    public static Optional<PhotonicsExtension> getPhotonics() {
        return ((PipelineManagerExt) Iris.getPipelineManager()).photonics();
    }

    public static int getBlockId(BlockState block) {
        var blockIds = WorldRenderingSettings.INSTANCE.getBlockStateIds();
        return blockIds == null ? -1 : blockIds.getOrDefault(block, -1);
    }

    public static IntSet getUsedBuffers() {
        return IntSet.of();
    }

    public static void bindBuffers(@Nullable WorldRenderingPipeline pipeline, int programId) {
        if (pipeline instanceof IrisRenderingPipeline ext)
            ((IrisRenderingPipelineExt) ext).photonics$bufferHolder().bind(programId, IrisUtil.getUsedBuffers());
    }

    public static void bindBuffers(int programId) {
        bindBuffers(
                Iris.getPipelineManager().getPipelineNullable(),
                programId
        );
    }
}
