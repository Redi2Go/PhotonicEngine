package at.redi2go.photonics.core.rendering.world.tree;

import at.redi2go.photonics.core.model.VoxelEntry;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.registry.block.entry.SimpleBlockEntry;

public enum BlockMergeMode {
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
