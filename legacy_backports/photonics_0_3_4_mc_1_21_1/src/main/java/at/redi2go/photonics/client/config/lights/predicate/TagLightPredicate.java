package at.redi2go.photonics.client.config.lights.predicate;

import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public record TagLightPredicate(Block block, @Nullable CompoundTag nbt, Map<String, String> vagueProperties, int priority) implements LightPredicate {
   public TagLightPredicate {
      Objects.requireNonNull(block, "block was null");
      Objects.requireNonNull(vagueProperties, "vagueProperties was null");
   }

   @Override
   public boolean test(BlockInWorld block) {
      BlockState state = block.getState();
      if (!state.is(this.block())) {
         return false;
      }

      for (Entry<String, String> entry : this.vagueProperties.entrySet()) {
         Property<?> property = this.block().getStateDefinition().getProperty(entry.getKey());
         if (property == null) {
            return false;
         }

         Comparable<?> value = (Comparable<?>)property.getValue(entry.getValue()).orElse(null);
         if (value == null) {
            return false;
         }

         if (!value.equals(state.getValue(property))) {
            return false;
         }
      }

      if (this.nbt == null) {
         return true;
      }

      BlockEntity blockEntity = block.getEntity();
      return blockEntity == null ? false : isNbtEqual(this.nbt, blockEntity, block.getLevel());
   }

   static boolean isNbtEqual(CompoundTag nbt, BlockEntity blockEntity, LevelReader level) {
      return NbtUtils.compareNbt(nbt, blockEntity.saveWithFullMetadata(level.registryAccess()), true);
   }
}
