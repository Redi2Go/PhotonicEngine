package at.redi2go.photonics.client.config.lights.predicate;

import java.util.Objects;
import net.minecraft.commands.arguments.blocks.BlockStateParser.BlockResult;
import net.minecraft.commands.arguments.blocks.BlockStateParser.TagResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jetbrains.annotations.NonNls;

public record BasicLightPredicate(@NonNls Block block, int priority) implements LightPredicate {
   public BasicLightPredicate {
      Objects.requireNonNull(block, "block was null");
   }

   @Override
   public boolean test(BlockInWorld block) {
      return block.getState().is(this.block);
   }

   public static boolean isBasic(BlockResult blockResult) {
      return blockResult.properties().isEmpty() && (blockResult.nbt() == null || blockResult.nbt().isEmpty());
   }

   public static boolean isBasic(TagResult tagResult) {
      return tagResult.vagueProperties().isEmpty() && (tagResult.nbt() == null || tagResult.nbt().isEmpty());
   }
}
