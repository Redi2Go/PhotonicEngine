package at.redi2go.photonics.core.rendering.world.block;

import at.redi2go.photonics.core.rendering.world.bakery.VoxelConsumer;

public interface BlockVariantBuilder extends BlockEntry.Builder, VoxelConsumer, BlockProvider {
    void setRegion(short region);

    void propagateException(Throwable ex);
}
