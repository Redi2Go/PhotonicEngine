package at.redi2go.photonics.core.config.lights.block;

import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlock;
import at.redi2go.photonics.api.mc.world.level.ILevelReader;
import at.redi2go.photonics.core.config.Variable;
import at.redi2go.photonics.core.config.lights.predicate.LightPredicate;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.List;
import java.util.stream.Collectors;

public class LightBlockVariable extends Variable<LightBlock> implements LightBlock {
    private final int priority;

    protected LightBlockVariable(String name, int priority) {
        super(name, LightBlock.TYPE);

        this.priority = priority;
    }

    @Override
    public List<LightPredicate> listPredicates() throws CommandSyntaxException {
        var actual = actual().listPredicates();
        if (priority != LightPredicate.DEFAULT_PRIORITY) {
            return actual.stream()
                    .map(e -> new PredicateWrapper(priority, e))
                    .collect(Collectors.toUnmodifiableList());
        }

        return actual;
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
