package at.redi2go.photonics.game.minecraft.world.level;

import at.redi2go.photonics.game.minecraft.Id;
import at.redi2go.photonics.game.minecraft.world.level.block.state.IStateDefinition;

import java.util.Optional;

public interface IBlock {
    Id ph$id();

    IStateDefinition<IBlock, IBlockState> ph$stateDefinition();

    IBlockState ph$defaultBlockState();

    static Optional<IBlock> fromId(Id id) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IBlock fromIdOrThrow(Id id) {
        return fromId(id).orElseThrow();
    }
}
