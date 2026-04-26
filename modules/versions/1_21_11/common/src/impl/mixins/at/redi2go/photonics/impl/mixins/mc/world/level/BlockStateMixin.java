package at.redi2go.photonics.impl.mixins.mc.world.level;

import at.redi2go.photonics.api.mc.IProperty;
import at.redi2go.photonics.api.mc.world.level.IBlock;
import at.redi2go.photonics.api.mc.world.level.IBlockState;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockState.class)
@SuppressWarnings("unchecked")
public abstract class BlockStateMixin extends BlockBehaviour.BlockStateBase implements IBlockState {
    private BlockStateMixin(
            Block block,
            Reference2ObjectArrayMap<Property<?>, Comparable<?>> reference2ObjectArrayMap,
            MapCodec<BlockState> mapCodec
    ) {
        super(block, reference2ObjectArrayMap, mapCodec);
    }

    @Override
    public IBlock block() {
        return (IBlock) getBlock();
    }

    @Override
    public boolean hasProperty(IProperty<?> property) {
        return hasProperty((Property<?>) property);
    }

    @Override
    public <T extends Comparable<T>> T getValue(IProperty<T> property) {
        return getValue((Property<T>) property);
    }
}
