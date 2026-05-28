package at.redi2go.photonics.core.config.lights.predicate;
import at.redi2go.photonics.api.mc.commands.arguments.blocks.IBlockStateParser;
import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlock;
import at.redi2go.photonics.api.mc.world.level.ILevelReader;
import org.jetbrains.annotations.NonNls;

import java.util.Objects;

public record BasicLightPredicate(
        @NonNls IBlock block,
        int priority
) implements LightPredicate {
    public BasicLightPredicate {
        Objects.requireNonNull(block, "block was null");
    }

    @Override
    public boolean test(@NonNls IBlockPos pos, @NonNls ILevelReader levelReader) {
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
