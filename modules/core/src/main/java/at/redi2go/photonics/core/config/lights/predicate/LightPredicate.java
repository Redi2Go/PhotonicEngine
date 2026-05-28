package at.redi2go.photonics.core.config.lights.predicate;

import at.redi2go.photonics.api.mc.commands.arguments.blocks.IBlockStateParser;
import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.core.registries.Registries;
import at.redi2go.photonics.api.mc.world.level.IBlock;
import at.redi2go.photonics.api.mc.world.level.ILevelReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jetbrains.annotations.NonNls;

import java.util.List;
import java.util.stream.Collectors;

public interface LightPredicate extends Comparable<LightPredicate> {
    /**
     * The default priority for non-basic predicates
     */
    int NORMAL_PRIORITY = 0;

    /**
     * The default priority
     */
    int DEFAULT_PRIORITY = Integer.MIN_VALUE;

    @NonNls
    IBlock block();

    int priority();

    /**
     * Returns {@code true} if {@code pos} passes this predicate.
     * This method does not test if the chunk at {@code pos} is loaded.
     */
    boolean test(@NonNls IBlockPos pos, @NonNls ILevelReader levelReader);

    @Override
    default int compareTo(@NonNls LightPredicate o) {
        return Integer.compare(this.priority(), o.priority());
    }

    static List<LightPredicate> parse(String value, int priority) throws CommandSyntaxException {
        return IBlockStateParser.parse(
                Registries.block(),
                new StringReader(value),
                true
        ).map(
                blockResult -> fromBlockResult(priority, blockResult),
                tagResult -> fromTagResult(priority, tagResult)
        );
    }

    private static List<LightPredicate> fromBlockResult(int priority, IBlockStateParser.BlockResult blockResult) {
        if (BasicLightPredicate.isBasic(blockResult) && priority == DEFAULT_PRIORITY) {
            return List.of(new BasicLightPredicate(blockResult.ph$blockState().ph$block(), priority));
        }

        final int adjustedPriority;

        if (priority == DEFAULT_PRIORITY)
            adjustedPriority = NORMAL_PRIORITY; // Sets priority for LightPredicateImpl
        else
            adjustedPriority = priority;

        return List.of(
                new LightPredicateImpl(
                        blockResult.ph$blockState(),
                        List.copyOf(blockResult.ph$properties().keySet()),
                        blockResult.ph$nbt(),
                        adjustedPriority
                )
        );
    }

    private static List<LightPredicate> fromTagResult(int priority, IBlockStateParser.TagResult tagResult) {
        if (BasicLightPredicate.isBasic(tagResult) && priority == DEFAULT_PRIORITY) {
            return tagResult.ph$tag()
                    .ph$stream()
                    .map(holder -> new BasicLightPredicate(holder.ph$value(), priority))
                    .collect(Collectors.toUnmodifiableList());
        }

        final int adjustedPriority;

        if (priority == DEFAULT_PRIORITY)
            adjustedPriority = NORMAL_PRIORITY; // Sets priority for LightPredicateImpl
        else
            adjustedPriority = priority;

        return tagResult.ph$tag()
                .ph$stream()
                .map(holder -> new TagLightPredicate(
                        holder.ph$value(),
                        tagResult.ph$nbt(),
                        tagResult.ph$vagueProperties(),
                        adjustedPriority))
                .collect(Collectors.toUnmodifiableList());
    }
}
