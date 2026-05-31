package at.redi2go.photonics.core.old.world.compiler;

import at.redi2go.photonics.core.old.world.block.BlockModel;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class BlockSorter {
    private final Int2ObjectSortedMap<List<Entry>> blocks = new Int2ObjectRBTreeMap<>(Comparator.reverseOrder());

    public void reset() {
        blocks.values().forEach(List::clear);
    }

    public void addBlock(
            int x, int y, int z,
            BlockModel block
    ) {
        blocks.computeIfAbsent(block.parts().size(), (i) -> new ArrayList<>())
                .add(new Entry(x, y, z, block));
    }

    public void addBlock(Vector3i pos, BlockModel block) {
        addBlock(
                pos.x(), pos.y(), pos.z(),
                block
        );
    }

    public void forEachBlock(Consumer<Entry> consumer) {
        for (var entry : blocks.int2ObjectEntrySet()) {
            for (var block : entry.getValue())
                consumer.accept(block);
        }
    }

    public record Entry(int x, int y, int z, BlockModel blockModel) {

    };
}
