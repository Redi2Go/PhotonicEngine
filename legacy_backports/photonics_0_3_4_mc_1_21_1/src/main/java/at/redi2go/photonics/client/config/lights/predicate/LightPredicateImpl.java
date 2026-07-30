package at.redi2go.photonics.client.config.lights.predicate;

import java.util.List;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

public record LightPredicateImpl(@NonNls BlockState blockState, @NonNls List<Property<?>> properties, @Nullable CompoundTag nbt, int priority)
   implements LightPredicate {
   public LightPredicateImpl {
      Objects.requireNonNull(blockState, "blockState was null");
      Objects.requireNonNull(properties, "properties was null");
   }

   @Override
   public Block block() {
      return this.blockState.getBlock();
   }

   @Override
   public boolean test(BlockInWorld block) {
      BlockState state = block.getState();
      if (!state.is(this.block())) {
         return false;
      }

      for (Property<?> property : this.properties) {
         if (!this.blockState.getValue(property).equals(state.getValue(property))) {
            return false;
         }
      }

      if (this.nbt == null) {
         return true;
      }

      BlockEntity blockEntity = block.getEntity();
      return blockEntity == null ? false : TagLightPredicate.isNbtEqual(this.nbt, blockEntity, block.getLevel());
   }
}
