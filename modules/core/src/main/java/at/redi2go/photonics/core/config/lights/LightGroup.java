package at.redi2go.photonics.core.config.lights;

import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlock;
import at.redi2go.photonics.api.mc.world.level.ILevelReader;
import at.redi2go.photonics.core.config.Variable;
import at.redi2go.photonics.core.config.lights.block.LightBlock;
import at.redi2go.photonics.core.config.lights.color.LightColor;
import at.redi2go.photonics.core.config.lights.falloff.LightFalloff;
import at.redi2go.photonics.core.config.lights.intensity.LightIntensity;
import at.redi2go.photonics.core.config.lights.predicate.LightPredicate;
import at.redi2go.photonics.core.config.lights.radius.LightRadius;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LightGroup {
    @Nullable LightColor color;
    @Nullable LightIntensity intensity;
    @Nullable LightRadius radius;
    @Nullable LightFalloff falloff;

    @Nullable Boolean isTraced;

    @Nullable List<LightBlock> blocks;
    @Nullable LinkedHashMap<String, LightGroup> overrides;

    public void recordLights(
            Variable.Owner owner,
            LightRegistry lights,
            Map<IBlock, Boolean> tracedLightsOverrides,
            int priority
    ) {
        recordLightsImpl(
                owner,
                lights,
                tracedLightsOverrides,
                null,
                null,
                null,
                null,
                null,
                priority
        );
    }

    private void recordLightsImpl(
            Variable.Owner owner,
            LightRegistry lights,
            Map<IBlock, Boolean> tracedLightsOverrides,
            @Nullable LightColor color,
            @Nullable Float intensity,
            @Nullable Float radius,
            @Nullable Float falloff,
            @Nullable Boolean isTraced,
            int priority
    ) {
        if (this.color != null) color = setOwner(this.color, owner);
        if (this.intensity != null) intensity = setOwner(this.intensity, owner).get();
        if (this.radius != null) radius = setOwner(this.radius, owner).get();
        if (this.falloff != null) falloff = setOwner(this.falloff, owner).get();
        if (this.isTraced != null) isTraced = this.isTraced;

        if (blocks != null) {
            for (final var block : blocks) {
                try {
                    for (final var predicate : setOwner(block, owner).listPredicates()) {
                        addBlock(
                                lights,
                                tracedLightsOverrides,
                                predicate,
                                color,
                                intensity,
                                radius,
                                falloff,
                                isTraced,
                                priority
                        );
                    }
                } catch (CommandSyntaxException e) {
                    // Could mean the predicate doesn't exist.
                    // Ignored so shaders can supply colors for modded blocks.
                }
            }
        }

        if (overrides != null) {
            for (final var override : overrides.values())
                override.recordLightsImpl(
                        owner,
                        lights,
                        tracedLightsOverrides,
                        color,
                        intensity,
                        radius,
                        falloff,
                        isTraced,
                        priority
                );
        }
    }

    private static <T> T setOwner(T value, Variable.Owner owner) {
        if (value instanceof Variable<?> variable)
            variable.setOwner(owner);

        return value;
    }

    private static void addBlock(
            LightRegistry lights,
            Map<IBlock, Boolean> tracedLightsOverrides,
            LightPredicate predicate,
            @Nullable LightColor color,
            @Nullable Float intensity,
            @Nullable Float radius,
            @Nullable Float falloff,
            @Nullable Boolean isTraced,
            int priority
    ) {
        final var block = predicate.block();
        final var blockString = block.ph$id().toString();

        Objects.requireNonNull(color, "no light color for" + blockString);
        Objects.requireNonNull(intensity, "no intensity for" + blockString);
        Objects.requireNonNull(radius, "no radius for" + blockString);
        Objects.requireNonNull(falloff, "no falloff for" + blockString);
        Objects.requireNonNull(isTraced, "no isTraced for" + blockString);

        if (priority != 0) {
            predicate = new PredicateWrapper(predicate.priority() + priority, predicate);
        }

        lights.add(
                new BlockLightInfo(
                        predicate,
                        color,
                        intensity,
                        radius,
                        falloff,
                        evaluateIsTraced(isTraced, block, tracedLightsOverrides),
                        isTraced
                )
        );
    }

    private static boolean evaluateIsTraced(
            boolean actual,
            IBlock block,
            Map<IBlock, Boolean> tracedLightsOverrides
    ) {
        Boolean value = tracedLightsOverrides.get(block);
        if (value == null) return actual;

        return value;
    }

    private record PredicateWrapper(int priority, LightPredicate actual) implements LightPredicate {
        @Override
        public IBlock block() {
            return actual.block();
        }

        @Override
        public boolean test(IBlockPos pos, ILevelReader levelReader) {
            return actual.test(pos, levelReader);
        }
    }
}
