package at.redi2go.photonics.api.mc.world.level;

import at.redi2go.photonics.api.mc.IProperty;
import at.redi2go.photonics.api.mc.core.IBlockPos;

public interface IBlockState {
    IBlock ph$block();

    default boolean ph$is(IBlock block) {
        return ph$block() == block;
    }

    boolean ph$isAir();

    boolean ph$isSuffocating(IBlockGetter blockGetter, IBlockPos blockPos);

    boolean ph$isCollisionShapeFullBlock(IBlockGetter blockGetter, IBlockPos blockPos);

    boolean ph$hasProperty(IProperty<?> property);

    <T extends Comparable<T>> T ph$getValue(IProperty<T> property);
}
