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
        return levelReader.getBlockState(pos).is(this.block);
    }

    @SuppressWarnings("DataFlowIssue") // nbt is immutable
    public static boolean isBasic(IBlockStateParser.BlockResult blockResult) {
        return blockResult.properties().isEmpty() && (blockResult.nbt() == null || blockResult.nbt().isEmpty());
    }

    @SuppressWarnings("DataFlowIssue") // nbt is immutable
    public static boolean isBasic(IBlockStateParser.TagResult tagResult) {
        return tagResult.vagueProperties().isEmpty() && (tagResult.nbt() == null || tagResult.nbt().isEmpty());
    }
}
