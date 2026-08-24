package at.redi2go.photonics.impl.minecraft;

import at.redi2go.photonics.game.minecraft.IProperty;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(Property.class)
public abstract class PropertyImpl<T extends Comparable<T>> implements IProperty<T> {
    @Shadow
    public abstract Optional<T> getValue(String string);

    @Override
    public Optional<T> ph$getValue(String name) {
        return getValue(name);
    }
}
