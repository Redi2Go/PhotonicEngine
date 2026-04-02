package at.redi2go.photonics.api.mc.world.level;

import at.redi2go.photonics.api.mc.IProperty;

import java.util.Map;

public interface IBlockState {
    IBlock block();

    default boolean is(IBlock block) {
        return block() == block;
    }

    boolean hasProperty(IProperty<?> property);

    <T extends Comparable<T>> T getValue(IProperty<T> property);
}
