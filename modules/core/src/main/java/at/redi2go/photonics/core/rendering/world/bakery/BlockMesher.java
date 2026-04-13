package at.redi2go.photonics.core.rendering.world.bakery;

import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlockAndTintGetter;
import at.redi2go.photonics.api.mc.world.level.IBlockState;

public interface BlockMesher {
    /**
     * Meshes a block at {@code pos} with {@code blockState}.
     *
     * @apiNote {@code VertexBuilder} only accepts quads
     */
    void meshBlock(
            IBlockPos pos,
            IBlockState blockState,
            IBlockAndTintGetter blockAndTintGetter,
            VertexBuilder vertexBuilder
    );
}
