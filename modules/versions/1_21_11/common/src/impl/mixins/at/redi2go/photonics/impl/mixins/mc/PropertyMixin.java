package at.redi2go.photonics.impl.mixins.mc;

import at.redi2go.photonics.api.mc.IProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Property.class)
@Implements(@Interface(iface = IProperty.class, prefix = "ph$"))
public interface PropertyMixin {
}
