package at.redi2go.photonics.impl.mixins.mc.commands.arguments.blocks;

import at.redi2go.photonics.api.mc.IProperty;
import at.redi2go.photonics.api.mc.commands.arguments.blocks.IBlockStateParser;
import at.redi2go.photonics.api.mc.nbt.ICompoundTag;
import at.redi2go.photonics.api.mc.world.level.IBlockState;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(BlockStateParser.BlockResult.class)
public abstract class BlockStateParser$BlockResultMixin implements IBlockStateParser.BlockResult {
    @Shadow public abstract BlockState shadow$blockState();

    @Override
    public IBlockState blockState() {
        return (IBlockState) shadow$blockState();
    }

    @Shadow public abstract Map<Property<?>, Comparable<?>> shadow$properties();

    @Override
    public Map<IProperty<?>, Comparable<?>> properties() {
        return (Map<IProperty<?>, Comparable<?>>) (Map<?, ?>) shadow$properties();
    }

    @Shadow public abstract CompoundTag shadow$nbt();

    @Override
    public @Nullable ICompoundTag nbt() {
        return (ICompoundTag) (Object) shadow$nbt();
    }
}
