package at.redi2go.photonics.core.rendering.world.block.palette;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;

public class TintBuilder extends IntArraySet {
    private long tintHash = 0;

    public TintBuilder() {
        super(8);
    }

    public boolean add(int tint) {
        if (super.add(tint)) {
            tintHash = tintHash * 31 + tint;
            return true;
        }

        return false;
    }

    public Result build() {
        return new Result(this, tintHash);
    }

    public record Result(IntArraySet tints, long hash) {
        public Int2IntMap indexes() {
            var result = new Int2IntOpenHashMap();

            int i = 0;
            for (var itr = tints.intIterator(); itr.hasNext(); )
                result.put(itr.nextInt(), i++);

            return result;
        }
    }
}
