package at.redi2go.photonics.core.config.lights.predicate;

import at.redi2go.photonics.game.minecraft.IProperty;
import at.redi2go.photonics.game.minecraft.core.IBlockPos;
import at.redi2go.photonics.game.minecraft.nbt.ICompoundTag;
import at.redi2go.photonics.game.minecraft.world.level.IBlock;
import at.redi2go.photonics.game.minecraft.world.level.IBlockState;
import at.redi2go.photonics.game.minecraft.world.level.ILevelReader;
import at.redi2go.photonics.game.minecraft.world.level.block.IBlockEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public record LightPredicateImpl(
        @NonNull IBlockState blockState,
        @NonNull List<IProperty<?>> properties,
        @Nullable ICompoundTag nbt,
        int priority
) implements LightPredicate {
    public LightPredicateImpl {
        Objects.requireNonNull(blockState, "blockState was null");
        Objects.requireNonNull(properties, "properties was null");
    }

    @Override
    public IBlock block() {
        return blockState.ph$block();
    }

    @Override
    public boolean test(@NonNull IBlockPos pos, @NonNull ILevelReader levelReader) {
        // Copied from BlockPredicateArgument.BlockPredicate

        final IBlockState state = levelReader.ph$getBlockState(pos);
        if (!state.ph$is(block())) return false;

        for (IProperty<?> property : properties) {
            if (!blockState.ph$hasProperty(property) || !blockState.ph$getValue(property).equals(state.ph$getValue(property)))
                return false;
        }

        if (nbt == null) return true;

        final IBlockEntity blockEntity = levelReader.ph$getBlockEntity(pos);
        if (blockEntity == null) return false;

        return ICompoundTag.isEqual(
                nbt,
                blockEntity.ph$saveWithFullMetadata(levelReader.ph$registryAccess())
        );
    }
}
