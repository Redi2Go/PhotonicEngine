package at.redi2go.photonics.common.meshing;

import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlockAndTintGetter;
import at.redi2go.photonics.api.mc.world.level.IBlockState;
import at.redi2go.photonics.core.rendering.world.bakery.BlockBuilder;
import at.redi2go.photonics.core.rendering.world.bakery.BlockMesher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3i;

public class MinecraftBlockMesher implements BlockMesher<McMeshState> {
    private static final ThreadLocal<McBlockRenderer> RENDERERS = ThreadLocal.withInitial(McBlockRenderer::new);

    @Override
    public void setup() {
        ModelBlockRenderer.enableCaching();
    }

    @Override
    public void teardown() {
        ModelBlockRenderer.clearCache();
    }

    @Override
    public McMeshState extractMeshState(
            Vector3i blockChunkOffset,
            IBlockPos pos,
            IBlockState blockState,
            IBlockAndTintGetter blockAndTintGetter
    ) {
        return RENDERERS.get().extractMeshState(
                blockChunkOffset,
                (BlockPos) pos,
                (BlockState) blockState,
                (BlockAndTintGetter) blockAndTintGetter
        );
    }

    @Override
    public void meshBlock(
            McMeshState meshState,
            Vector3i blockChunkOffset,
            IBlockPos pos,
            IBlockState blockState,
            IBlockAndTintGetter blockAndTintGetter,
            BlockBuilder blockBuilder
    ) {
        RENDERERS.get().meshBlock(
                meshState,
                blockChunkOffset,
                (BlockPos) pos,
                (BlockState) blockState,
                (BlockAndTintGetter) blockAndTintGetter,
                blockBuilder
        );
    }
}
