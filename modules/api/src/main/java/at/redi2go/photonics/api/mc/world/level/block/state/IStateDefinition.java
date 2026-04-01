package at.redi2go.photonics.api.mc.world.level.block.state;

import at.redi2go.photonics.api.mc.IProperty;
import org.jetbrains.annotations.Nullable;

public interface IStateDefinition<K, V> {
    @Nullable IProperty<?> getProperty(String string);
}
