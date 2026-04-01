package at.redi2go.photonics.api.mc.world.level;

import at.redi2go.photonics.api.mc.Id;
import at.redi2go.photonics.api.mc.world.level.block.state.IStateDefinition;

public interface IBlock {
    Id id();

    IStateDefinition<IBlock, IBlockState> stateDefinition();

    IBlockState defaultBlockState();

    static IBlock fromId(Id id) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
