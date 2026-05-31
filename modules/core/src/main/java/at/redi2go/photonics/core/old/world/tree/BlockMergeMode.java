package at.redi2go.photonics.core.old.world.tree;

import at.redi2go.photonics.core.old.model.VoxelEntry;
import at.redi2go.photonics.core.old.world.block.BlockEntry;

public enum  BlockMergeMode {
    OVERWRITE {
        @Override
        public VoxelEntry merge(BlockEntry oldEntry, BlockEntry newEntry) {
            if (newEntry.boundingVolume() > oldEntry.boundingVolume())
                return newEntry;

            newEntry.close();
            return oldEntry;
        }
    },
    COMBINE {
        @Override
        public VoxelEntry merge(BlockEntry oldEntry, BlockEntry newEntry) {
            return oldEntry.merge(newEntry);
        }
    };

    public abstract VoxelEntry merge(BlockEntry oldEntry, BlockEntry newEntry);
}
