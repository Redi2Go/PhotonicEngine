package at.redi2go.photonics.core.rendering.world.tree;

import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import org.jetbrains.annotations.Nullable;

public enum  BlockMergeMode {
    OVERWRITE {
        @Override
        public BlockEntry merge(@Nullable BlockEntry oldEntry, BlockEntry newEntry) {
            //TODO: KEEP TRACK OF ALL BLOCKS
            if (oldEntry == null || newEntry.boundingVolume() > oldEntry.boundingVolume())
                return newEntry;

            newEntry.close();
            return oldEntry;
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
