package at.redi2go.photonics.client.config.lights.predicate;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.commands.arguments.blocks.BlockStateParser.BlockResult;
import net.minecraft.commands.arguments.blocks.BlockStateParser.TagResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public interface LightPredicate extends Comparable<LightPredicate> {
   int NORMAL_PRIORITY = 0;
   int DEFAULT_PRIORITY = Integer.MIN_VALUE;

   @NonNls
   Block block();

   int priority();

   boolean test(@NonNls BlockInWorld var1);

   default int compareTo(@NotNull LightPredicate o) {
      return Integer.compare(this.priority(), o.priority());
   }

   static List<LightPredicate> parse(String value, int priority) throws CommandSyntaxException {
      return (List<LightPredicate>)BlockStateParser.parseForTesting(BuiltInRegistries.BLOCK.asLookup(), new StringReader(value), true)
         .map(blockResult -> fromBlockResult(priority, blockResult), tagResult -> fromTagResult(priority, tagResult));
   }

   private static List<LightPredicate> fromBlockResult(int priority, BlockResult blockResult) {
      if (BasicLightPredicate.isBasic(blockResult) && priority == Integer.MIN_VALUE) {
         return List.of(new BasicLightPredicate(blockResult.blockState().getBlock(), priority));
      }

      int adjustedPriority;
      if (priority == Integer.MIN_VALUE) {
         adjustedPriority = 0;
      } else {
         adjustedPriority = priority;
      }

      return List.of(new LightPredicateImpl(blockResult.blockState(), List.copyOf(blockResult.properties().keySet()), blockResult.nbt(), adjustedPriority));
   }

   private static List<LightPredicate> fromTagResult(int priority, TagResult tagResult) {
      if (BasicLightPredicate.isBasic(tagResult) && priority == Integer.MIN_VALUE) {
         return tagResult.tag().stream().map(holder -> new BasicLightPredicate((Block)holder.value(), priority)).collect(Collectors.toUnmodifiableList());
      }

      int adjustedPriority;
      if (priority == Integer.MIN_VALUE) {
         adjustedPriority = 0;
      } else {
         adjustedPriority = priority;
      }

      return tagResult.tag()
         .stream()
         .map(holder -> new TagLightPredicate((Block)holder.value(), tagResult.nbt(), tagResult.vagueProperties(), adjustedPriority))
         .collect(Collectors.toUnmodifiableList());
   }
}
