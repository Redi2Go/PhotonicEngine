package at.redi2go.photonics.impl.minecraft.world.level;

import at.redi2go.photonics.game.minecraft.Id;
import at.redi2go.photonics.game.minecraft.world.level.IBlock;
import at.redi2go.photonics.game.minecraft.world.level.IBlockState;
import at.redi2go.photonics.game.minecraft.world.level.block.state.IStateDefinition;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(Block.class)
public abstract class BlockImpl implements IBlock {
    @Shadow
    public abstract StateDefinition<Block, BlockState> getStateDefinition();

    @Shadow
    public abstract BlockState defaultBlockState();

    @Override
    @SuppressWarnings("DataFlowIssue")
    public Id ph$id() {
        return (Id) (Object) BuiltInRegistries.BLOCK.getKey((Block) (Object) this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public IStateDefinition<IBlock, IBlockState> ph$stateDefinition() {
        return (IStateDefinition<IBlock, IBlockState>) getStateDefinition();
    }

    @Override
    public IBlockState ph$defaultBlockState() {
        return (IBlockState) defaultBlockState();
    }

    @Mixin(IBlock.class)
    public interface StaticMethods {
        @Overwrite
        @SuppressWarnings({"unchecked", "rawtypes", "DataFlowIssue"})
        static Optional<IBlock> fromId(Id id) {
            return (Optional) BuiltInRegistries.BLOCK
                    .get((Identifier) (Object) id)
                    .map(Holder.Reference::value);
        }
    }
}
