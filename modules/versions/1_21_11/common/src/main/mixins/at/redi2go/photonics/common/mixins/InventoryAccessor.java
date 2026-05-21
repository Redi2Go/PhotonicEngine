package at.redi2go.photonics.common.mixins;

import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Inventory.class)
public interface InventoryAccessor {
    @Accessor
    EntityEquipment getEquipment();
}
