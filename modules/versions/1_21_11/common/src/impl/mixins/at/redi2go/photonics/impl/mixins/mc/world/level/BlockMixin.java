package at.redi2go.photonics.impl.mixins.mc.world.level;

import at.redi2go.photonics.api.mc.Id;
import at.redi2go.photonics.api.mc.world.level.IBlock;
import at.redi2go.photonics.api.mc.world.level.IBlockState;
import at.redi2go.photonics.api.mc.world.level.block.state.IStateDefinition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Block.class)
@SuppressWarnings("unchecked")
public abstract class BlockMixin implements IBlock {
    @Shadow
    public abstract StateDefinition<Block, BlockState> getStateDefinition();

    @Shadow
    public abstract BlockState shadow$defaultBlockState();

    @Override
    public Id id() {
        return (Id) (Object) BuiltInRegistries.BLOCK.getKey((Block) (Object) this);
    }

    @Override
    public IStateDefinition<IBlock, IBlockState> stateDefinition() {
        return (IStateDefinition<IBlock, IBlockState>) getStateDefinition();
    }

    @Override
    public IBlockState defaultBlockState() {
        return (IBlockState) shadow$defaultBlockState();
    }
}
