package at.redi2go.photonics.core.config.lights.block;

import at.redi2go.photonics.core.config.lights.predicate.LightPredicate;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.List;
import java.util.Objects;

public record DefaultLightBlock(String value) implements LightBlock {
    public DefaultLightBlock {
        Objects.requireNonNull(value, "value was null");
    }

    @Override
    public List<LightPredicate> listPredicates() throws CommandSyntaxException {
        return LightPredicate.parse(value, LightPredicate.DEFAULT_PRIORITY);
    }
}
