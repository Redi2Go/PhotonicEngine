package at.redi2go.photonics.impl.mixins.mc.commands.arguments.blocks;

import at.redi2go.photonics.api.mc.commands.arguments.blocks.IBlockStateParser;
import at.redi2go.photonics.api.mc.core.IHolderSet;
import at.redi2go.photonics.api.mc.nbt.ICompoundTag;
import at.redi2go.photonics.api.mc.world.level.IBlock;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.HolderSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("unchecked")
@Mixin(BlockStateParser.TagResult.class)
public abstract class BlockStateParser$TagResultMixin implements IBlockStateParser.TagResult {
    @Shadow public abstract HolderSet<Block> shadow$tag();

    @Override
    public IHolderSet<IBlock> tag() {
        return (IHolderSet<IBlock>) (Object) shadow$tag();
    }

    @Shadow public abstract CompoundTag shadow$nbt();

    @Override
    public @Nullable ICompoundTag nbt() {
        return (ICompoundTag) (Object) shadow$nbt();
    }
}
