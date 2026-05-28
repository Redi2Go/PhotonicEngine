package at.redi2go.photonics.impl.mixins.mc;

import at.redi2go.photonics.api.mc.IProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(Property.class)
public abstract class PropertyMixin<T extends Comparable<T>> implements IProperty<T> {
    @Shadow
    public abstract Optional<T> getValue(String string);

    @Override
    public Optional<T> ph$getValue(String name) {
        return getValue(name);
    }
}
