package at.redi2go.photonics.game.mc.world.level.block.state;

import at.redi2go.photonics.game.mc.IProperty;
import org.jetbrains.annotations.Nullable;

public interface IStateDefinition<K, V> {
    @Nullable IProperty<?> ph$getProperty(String string);
}
