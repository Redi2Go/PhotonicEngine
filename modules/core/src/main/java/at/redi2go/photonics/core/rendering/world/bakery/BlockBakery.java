package at.redi2go.photonics.core.rendering.world.bakery;

import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlockAndTintGetter;
import at.redi2go.photonics.api.mc.world.level.IBlockState;
import at.redi2go.photonics.core.rendering.world.WorldOrigin;

public interface BlockBakery {
    void reset();

    void setRegion(short region);

    //TODO: Make relative to chunk
    void submitBlock(
            WorldOrigin origin,
            IBlockPos pos,
            IBlockState blockState,
            IBlockAndTintGetter blockAndTintGetter
    );

    void bake(VoxelConsumer voxelConsumer, BlockConsumer blockConsumer);
}
