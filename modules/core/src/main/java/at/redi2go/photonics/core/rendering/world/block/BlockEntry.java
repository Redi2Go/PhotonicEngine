package at.redi2go.photonics.core.rendering.world.block;

import at.redi2go.photonics.core.rendering.world.ReferencedObject;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import org.jetbrains.annotations.Nullable;

public interface BlockEntry extends ReferencedObject {
    int memory();

    Builder createBuilder();

    /**
     * Create a builder with voxels from {@code regions} being discarded,
     * or null if the new builder would be empty.
     *
     * @param regions List of regions to clear
     */
    @Nullable Builder clearRegions(short[] regions);

    interface Builder extends BlockEntry {
        boolean insert(
                int x, int y, int z,
                int region,
                int normal,
                TextureData textureData
        );

        @Override
        default Builder createBuilder() {
            return this;
        }

        /**
         * Builds the block entry and automatically acquires a reference.
         */
        BlockEntry build();
    }
}
