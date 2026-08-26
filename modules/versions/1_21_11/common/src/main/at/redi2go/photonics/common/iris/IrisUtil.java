package at.redi2go.photonics.common.iris;

import at.redi2go.photonics.common.iris.pipeline.IrisPipelineManagerExt;
import at.redi2go.photonics.game.minecraft.Id;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.minecraft.world.level.block.state.BlockState;

public class IrisUtil {
    public static int getBlockId(BlockState block) {
        var blockIds = WorldRenderingSettings.INSTANCE.getBlockStateIds();
        return blockIds == null ? -1 : blockIds.getOrDefault(block, -1);
    }

    public static Id getCurrentDimension() {
        NamespacedId irisDimension = Iris.getCurrentDimension();
        Id dimension = Id.fromNamespaceAndPath(irisDimension.getNamespace(), irisDimension.getName());

        return dimension;
    }

    public static IrisPipelineManagerExt getPipelineManager() {
        return (IrisPipelineManagerExt) Iris.getPipelineManager();
    }

    private IrisUtil() { }
}
