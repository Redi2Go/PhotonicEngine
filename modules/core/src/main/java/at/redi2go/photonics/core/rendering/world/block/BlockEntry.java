package at.redi2go.photonics.core.rendering.world.block;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jetbrains.annotations.Nullable;

public interface BlockEntry extends Disposable {
    int begin();

    int skylight();

    Builder createBuilder();

    /**
     * Create a builder with voxels from {@code regions} being discarded,
     * or null if the new builder would be empty.
     *
     * @param regions Set of regions to clear
     */
    @Nullable Builder clearRegions(ShortSet regions);

    interface Builder extends BlockEntry {
        void setSkylight(int skylight);

        boolean insert(
                int x, int y, int z,
                short region,
                int normal,
                int tint,
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
