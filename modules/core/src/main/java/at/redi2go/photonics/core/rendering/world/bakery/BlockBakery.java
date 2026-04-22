package at.redi2go.photonics.core.rendering.world.bakery;

import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlockAndTintGetter;
import at.redi2go.photonics.api.mc.world.level.IBlockState;
import at.redi2go.photonics.core.rendering.world.WorldOrigin;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

public interface BlockBakery {
    void reset();

    void setRegion(short region);

    void setChunkOffset(Vector3i chunkOffset);

    //TODO: Make relative to chunk
    void submitBlock(
            Vector3i blockChunkOffset,
            IBlockPos pos,
            IBlockState blockState,
            IBlockAndTintGetter blockAndTintGetter
    );

    void bake(VoxelConsumer voxelConsumer, BlockConsumer blockConsumer);
}
