package at.redi2go.photonics.core.config.lights.predicate;
import at.redi2go.photonics.game.minecraft.commands.arguments.blocks.IBlockStateParser;
import at.redi2go.photonics.game.minecraft.core.IBlockPos;
import at.redi2go.photonics.game.minecraft.world.level.IBlock;
import at.redi2go.photonics.game.minecraft.world.level.ILevelReader;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public record BasicLightPredicate(
        @NonNull IBlock block,
        int priority
) implements LightPredicate {
    public BasicLightPredicate {
        Objects.requireNonNull(block, "block was null");
    }

    @Override
    public boolean test(@NonNull IBlockPos pos, @NonNull ILevelReader levelReader) {
        return levelReader.ph$getBlockState(pos).ph$is(this.block);
    }

    @SuppressWarnings("DataFlowIssue") // nbt is immutable
    public static boolean isBasic(IBlockStateParser.BlockResult blockResult) {
        return blockResult.ph$properties().isEmpty() && (blockResult.ph$nbt() == null || blockResult.ph$nbt().ph$isEmpty());
    }

    @SuppressWarnings("DataFlowIssue") // nbt is immutable
    public static boolean isBasic(IBlockStateParser.TagResult tagResult) {
        return tagResult.ph$vagueProperties().isEmpty() && (tagResult.ph$nbt() == null || tagResult.ph$nbt().ph$isEmpty());
    }
}
