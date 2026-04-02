package at.redi2go.photonics.impl.mixins.mc.world.level;

import at.redi2go.photonics.api.mc.world.level.IBlockState;
import at.redi2go.photonics.api.mc.world.level.ILevelReader;
import at.redi2go.photonics.impl.mc.world.level.SingleBlockLevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@SuppressWarnings("DataFlowIssue")
@Mixin(value = ILevelReader.class, remap = false)
public interface ILevelReaderImpl {
    @Overwrite
    static ILevelReader createFacade(IBlockState blockState) {
        return (ILevelReader) new SingleBlockLevelReader((BlockState) blockState);
    }
}
