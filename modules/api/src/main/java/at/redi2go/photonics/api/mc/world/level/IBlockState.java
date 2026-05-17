package at.redi2go.photonics.api.mc.world.level;

import at.redi2go.photonics.api.mc.IProperty;
import at.redi2go.photonics.api.mc.core.IBlockPos;

public interface IBlockState {
    IBlock block();

    default boolean is(IBlock block) {
        return block() == block;
    }

    boolean isAir();

    boolean isSuffocating(IBlockGetter blockGetter, IBlockPos blockPos);

    boolean isCollisionShapeFullBlock(IBlockGetter blockGetter, IBlockPos blockPos);

    boolean hasProperty(IProperty<?> property);

    <T extends Comparable<T>> T getValue(IProperty<T> property);
}
