package at.redi2go.photonics.client.config.lights.block;

import at.redi2go.photonics.client.config.Variable;
import at.redi2go.photonics.client.config.lights.predicate.LightPredicate;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public class LightBlockVariable extends Variable<LightBlock> implements LightBlock {
   private final int priority;

   protected LightBlockVariable(String name, int priority) {
      super(name, LightBlock.TYPE);
      this.priority = priority;
   }

   @Override
   public List<LightPredicate> listPredicates() throws CommandSyntaxException {
      List<LightPredicate> actual = this.actual().listPredicates();
      return this.priority != Integer.MIN_VALUE
         ? actual.stream().map(e -> new LightBlockVariable.PredicateWrapper(this.priority, e)).collect(Collectors.toUnmodifiableList())
         : actual;
   }

   private record PredicateWrapper(int priority, LightPredicate actual) implements LightPredicate {
      @Override
      public Block block() {
         return this.actual.block();
      }

      @Override
      public boolean test(BlockInWorld block) {
         return this.actual.test(block);
      }
   }
}
