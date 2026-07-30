package at.redi2go.photonics.client.rendering.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.minecraft.world.level.block.state.BlockState;

public class IrisUtil {
   public static int getBlockId(BlockState block) {
      Object2IntMap<BlockState> blockIds = WorldRenderingSettings.INSTANCE.getBlockStateIds();
      return blockIds == null ? -1 : blockIds.getOrDefault(block, -1);
   }
}
