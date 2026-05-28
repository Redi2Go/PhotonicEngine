package at.redi2go.photonics.impl.mixins.mc.world.level;

import at.redi2go.photonics.api.mc.IProperty;
import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlock;
import at.redi2go.photonics.api.mc.world.level.IBlockGetter;
import at.redi2go.photonics.api.mc.world.level.IBlockState;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockState.class)
@SuppressWarnings("unchecked")
public abstract class BlockStateMixin extends BlockBehaviour.BlockStateBase implements IBlockState {
    @Override
    public IBlock ph$block() {
        return (IBlock) getBlock();
    }

    @Override
    public boolean ph$isAir() {
        return isAir();
    }

    @Override
    public boolean ph$isSuffocating(IBlockGetter blockGetter, IBlockPos blockPos) {
        return isSuffocating((BlockGetter) blockGetter, (BlockPos) blockPos);
    }

    @Override
    public boolean ph$isCollisionShapeFullBlock(IBlockGetter blockGetter, IBlockPos blockPos) {
        return isCollisionShapeFullBlock((BlockGetter) blockGetter, (BlockPos) blockPos);
    }

    @Override
    public boolean ph$hasProperty(IProperty<?> property) {
        return hasProperty((Property<?>) property);
    }

    @Override
    public <T extends Comparable<T>> T ph$getValue(IProperty<T> property) {
        return getValue((Property<T>) property);
    }

    private BlockStateMixin(
            Block block,
            Reference2ObjectArrayMap<Property<?>, Comparable<?>> reference2ObjectArrayMap,
            MapCodec<BlockState> mapCodec
    ) {
        super(block, reference2ObjectArrayMap, mapCodec);
    }
}
