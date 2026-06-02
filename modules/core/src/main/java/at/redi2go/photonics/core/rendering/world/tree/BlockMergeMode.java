package at.redi2go.photonics.core.rendering.world.tree;

import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryMemory;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.PriorityQueue;

public enum  BlockMergeMode {
    OVERWRITE {
        @Override
        public BlockEntry merge(@Nullable BlockEntry oldEntry, BlockEntry newEntry) {
            if (oldEntry == null) return newEntry;

            PriorityBlockEntry blockEntry = oldEntry instanceof PriorityBlockEntry po ? po : new PriorityBlockEntry(oldEntry);
            blockEntry.add(newEntry);

            return blockEntry;
        }

        private static class PriorityBlockEntry implements BlockEntry {
            private static final Comparator<BlockEntry> BLOCK_COMPARATOR = Comparator.comparing(BlockEntry::boundingVolume)
                    .reversed();

            private final IntSet regions = new IntOpenHashSet();
            private final PriorityQueue<BlockEntry> blockQueue = new PriorityQueue<>(BLOCK_COMPARATOR);

            private BlockEntry top;

            public PriorityBlockEntry(BlockEntry entry) {
                add(entry);
            }

            @Override
            public int boundingVolume() {
                return top == null ? 0 : top.boundingVolume();
            }

            @Override
            public IntSet regions() {
                return regions;
            }

            public void add(BlockEntry entry) {
                if (entry instanceof PriorityBlockEntry other) {
                    blockQueue.addAll(other.blockQueue);
                } else blockQueue.add(entry);


                top = blockQueue.peek();
                regions.addAll(entry.regions());
            }

            @Override
            public BlockEntry merge(BlockEntry entry) {
                add(entry);
                return this;
            }

            @Override
            public @Nullable VoxelTreeEntry removeRegions(IntSet regions) {
                blockQueue.removeIf((e) -> e.removeRegions(regions) == null);

                if (blockQueue.isEmpty()) return null;
                if (blockQueue.size() == 1) return blockQueue.peek();

                top = blockQueue.peek();
                return this;
            }

            @Override
            public void uploadTo(VoxelEntryMemory memory) {
                if (top == null) {
                    memory.setEntryFlag(false);
                    memory.setChildMask(0);

                    return;
                }

                top.uploadTo(memory);
            }

            @Override
            public void close() {
                while (!blockQueue.isEmpty())
                    blockQueue.poll().close();
            }
        }
    },
    COMBINE {
        @Override
        public BlockEntry merge(@Nullable BlockEntry oldEntry, BlockEntry newEntry) {
            return oldEntry == null ? newEntry : oldEntry.merge(newEntry);
        }
    };

    public abstract BlockEntry merge(@Nullable BlockEntry oldEntry, BlockEntry newEntry);
}
