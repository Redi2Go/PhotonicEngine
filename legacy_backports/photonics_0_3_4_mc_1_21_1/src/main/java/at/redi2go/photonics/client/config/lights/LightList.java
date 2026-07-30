package at.redi2go.photonics.client.config.lights;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import java.util.Comparator;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LightList {
   private final ListMultimap<Block, BlockLightInfo> lights = ArrayListMultimap.create();

   public Set<Block> keySet() {
      return this.lights.keySet();
   }

   public void clear() {
      this.lights.clear();
   }

   public void add(BlockLightInfo light) {
      this.lights.put(light.block(), light);
   }

   public void sort() {
      for (Block key : this.lights.keySet()) {
         this.lights.get(key).sort(Comparator.reverseOrder());
      }
   }

   @Nullable
   public BlockLightInfo get(BlockPos pos, LevelReader level) {
      for (BlockLightInfo light : this.lights.get(level.getBlockState(pos).getBlock())) {
         if (light.intensity() == 0.0F) {
            return null;
         }

         if (light.emitsLight(pos, level)) {
            return light;
         }
      }

      return null;
   }

   @Nullable
   public BlockLightInfo get(BlockState blockState) {
      return this.get(BlockPos.ZERO, new FakeLevelReader(blockState));
   }

   @Nullable
   public BlockLightInfo getDefault(Block block) {
      return this.get(block.defaultBlockState());
   }

   public boolean isTraced(Block block) {
      for (BlockLightInfo light : this.lights.get(block)) {
         if (light.requestedTrace()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public int hashCode() {
      return this.lights.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else {
         return obj instanceof LightList other ? other.lights.equals(this.lights) : false;
      }
   }
}
