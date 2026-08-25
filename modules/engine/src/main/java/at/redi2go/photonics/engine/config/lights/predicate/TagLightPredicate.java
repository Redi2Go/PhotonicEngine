package at.redi2go.photonics.engine.config.lights.predicate;

import at.redi2go.photonics.game.minecraft.IProperty;
import at.redi2go.photonics.game.minecraft.core.IBlockPos;
import at.redi2go.photonics.game.minecraft.nbt.ICompoundTag;
import at.redi2go.photonics.game.minecraft.world.level.IBlock;
import at.redi2go.photonics.game.minecraft.world.level.IBlockState;
import at.redi2go.photonics.game.minecraft.world.level.ILevelReader;
import at.redi2go.photonics.game.minecraft.world.level.block.IBlockEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public record TagLightPredicate(
        IBlock block,
        @Nullable ICompoundTag nbt,
        Map<String, String> vagueProperties,
        int priority
) implements LightPredicate {
    public TagLightPredicate {
        Objects.requireNonNull(block, "block was null");
        Objects.requireNonNull(vagueProperties, "vagueProperties was null");
    }

    @Override
    public boolean test(@NonNull IBlockPos pos, @NonNull ILevelReader levelReader) {
        // Copied from BlockPredicateArgument.TagPredicate

        final IBlockState state = levelReader.ph$getBlockState(pos);
        if (!state.ph$is(block())) return false;

        for (Map.Entry<String, String> entry : vagueProperties.entrySet()) {
            final IProperty<?> property = block().ph$stateDefinition().ph$getProperty(entry.getKey());
            if (property == null) return false;

            final var value = property.ph$getValue(entry.getValue()).orElse(null);
            if (value == null) return false;

            if (!value.equals(state.ph$getValue(property))) return false;
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
