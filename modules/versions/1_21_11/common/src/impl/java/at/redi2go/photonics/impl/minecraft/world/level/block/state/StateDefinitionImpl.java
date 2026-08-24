package at.redi2go.photonics.impl.minecraft.world.level.block.state;

import at.redi2go.photonics.game.minecraft.IProperty;
import at.redi2go.photonics.game.minecraft.world.level.block.state.IStateDefinition;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StateDefinition.class)
public abstract class StateDefinitionImpl<K, V> implements IStateDefinition<K, V> {
    @Shadow
    public abstract @Nullable Property<?> getProperty(String string);

    @Override
    public @Nullable IProperty<?> ph$getProperty(String string) {
        return (IProperty<?>) getProperty(string);
    }
}
