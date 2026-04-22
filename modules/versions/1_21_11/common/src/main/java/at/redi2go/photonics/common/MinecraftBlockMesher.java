package at.redi2go.photonics.common;

import at.redi2go.photonics.api.mc.Id;
import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlockAndTintGetter;
import at.redi2go.photonics.api.mc.world.level.IBlockState;
import at.redi2go.photonics.common.iris.IrisUtil;
import at.redi2go.photonics.core.rendering.world.WorldOrigin;
import at.redi2go.photonics.core.rendering.world.bakery.BlockMesher;
import at.redi2go.photonics.core.rendering.world.bakery.BlockBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;

public class MinecraftBlockMesher implements BlockMesher {
    private final ThreadLocal<Renderer> renderer = ThreadLocal.withInitial(Renderer::new);

    @Override
    public void meshBlock(
            Vector3i blockChunkOffset,
            IBlockPos pos,
            IBlockState blockState,
            IBlockAndTintGetter blockAndTintGetter,
            BlockBuilder blockBuilder
    ) {
        meshBlock(
                blockChunkOffset,
                (BlockPos) pos,
                (BlockState) blockState,
                (BlockAndTintGetter) blockAndTintGetter,
                blockBuilder
        );
    }

    private void meshBlock(
            Vector3i blockChunkOffset,
            BlockPos pos,
            BlockState blockState,
            BlockAndTintGetter blockAndTintGetter,
            BlockBuilder builder
    ) {
        var renderer = this.renderer.get();
        builder.beginBlock(IrisUtil.getBlockId(blockState), blockChunkOffset);

        FluidState fluidState = blockState.getFluidState();
        if (!fluidState.isEmpty()) renderer.submitFluid(
                pos,
                blockAndTintGetter,
                builder,
                blockState,
                fluidState
        );

        //TODO: Block entities (VERY FUN!!!!)

        if (blockState.getRenderShape() == RenderShape.MODEL) {
            renderer.submitBlock(
                    pos,
                    blockState,
                    blockAndTintGetter,
                    builder
            );
        }
    }

    private static class Renderer {
        private final RandomSource randomSource = RandomSource.create();
        private final List<BlockModelPart> parts = new ArrayList<>();
        private final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        private final PoseStack poseStack = new PoseStack();

        private static final Id BLOCK_ATLAS = (Id) (Object) TextureAtlas.LOCATION_BLOCKS;

        private void submitFluid(
                BlockPos blockPos,
                BlockAndTintGetter blockAndTintGetter,
                BlockBuilder builder,
                BlockState blockState,
                FluidState fluidState
        ) {
            builder.useAtlas(BLOCK_ATLAS);
            builder.useOffset(
                    -(blockPos.getX() & 15),
                    -(blockPos.getY() & 15),
                    -(blockPos.getZ() & 15)
            );

            blockRenderer.renderLiquid(blockPos, blockAndTintGetter, (VertexConsumer) builder, blockState, fluidState);
        }

        private void submitBlock(
                BlockPos pos,
                BlockState blockState,
                BlockAndTintGetter blockAndTintGetter,
                BlockBuilder builder
        ) {
            builder.useAtlas(BLOCK_ATLAS);
            builder.useOffset(0f, 0f, 0f);

            poseStack.pushPose();
            //poseStack.translate(blockState.getOffset(pos));

            parts.clear();
            randomSource.setSeed(blockState.getSeed(pos));
            blockRenderer.getBlockModel(blockState).collectParts(randomSource, parts);
            ((BlockRenderDispatcherExt) blockRenderer)
                    .photonics$modelBlockRenderer()
                    .tesselateWithoutAO(blockAndTintGetter, parts, blockState, pos, poseStack, (VertexConsumer) builder, false, OverlayTexture.NO_OVERLAY);

            poseStack.popPose();
        }
    }
}
