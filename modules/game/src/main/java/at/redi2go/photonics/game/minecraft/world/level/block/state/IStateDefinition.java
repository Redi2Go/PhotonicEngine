package at.redi2go.photonics.game.minecraft.world.level.block.state;

import at.redi2go.photonics.game.minecraft.IProperty;
import org.jetbrains.annotations.Nullable;

public interface IStateDefinition<K, V> {
    @Nullable IProperty<?> ph$getProperty(String string);
}
