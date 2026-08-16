package at.redi2go.photonics.impl.mixins.mc.commands.arguments.blocks;

import at.redi2go.photonics.game.minecraft.IProperty;
import at.redi2go.photonics.game.minecraft.commands.arguments.blocks.IBlockStateParser;
import at.redi2go.photonics.game.minecraft.nbt.ICompoundTag;
import at.redi2go.photonics.game.minecraft.world.level.IBlockState;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(BlockStateParser.BlockResult.class)
public abstract class BlockStateParser$BlockResultMixin implements IBlockStateParser.BlockResult{
    @Shadow
    public abstract BlockState blockState();

    @Shadow
    public abstract Map<Property<?>, Comparable<?>> properties();

    @Shadow
    public abstract @Nullable CompoundTag nbt();

    @Override
    public IBlockState ph$blockState() {
        return (IBlockState) blockState();
    }

    @Override
    public Map<IProperty<?>, Comparable<?>> ph$properties() {
        return (Map) properties();
    }

    @Override
    public ICompoundTag ph$nbt() {
        return (ICompoundTag) (Object) nbt();
    }
}
