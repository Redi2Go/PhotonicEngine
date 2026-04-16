package at.redi2go.photonics.common;

import at.redi2go.photonics.api.mc.Id;
import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlockAndTintGetter;
import at.redi2go.photonics.api.mc.world.level.IBlockState;
import at.redi2go.photonics.common.iris.IrisUtil;
import at.redi2go.photonics.core.rendering.world.WorldOrigin;
import at.redi2go.photonics.core.rendering.world.bakery.BlockMesher;
import at.redi2go.photonics.core.rendering.world.bakery.VertexBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.ArrayList;
import java.util.List;

public class MinecraftBlockMesher implements BlockMesher {
    private final ThreadLocal<Renderer> renderer = ThreadLocal.withInitial(Renderer::new);

    @Override
    public void meshBlock(
            WorldOrigin origin,
            IBlockPos pos,
            IBlockState blockState,
            IBlockAndTintGetter blockAndTintGetter,
            VertexBuilder vertexBuilder
    ) {
        meshBlock(
                origin,
                (BlockPos) pos,
                (BlockState) blockState,
                (BlockAndTintGetter) blockAndTintGetter,
                vertexBuilder
        );
    }

    private void meshBlock(
            WorldOrigin origin,
            BlockPos pos,
            BlockState blockState,
            BlockAndTintGetter blockAndTintGetter,
            VertexBuilder builder
    ) {
        var renderer = this.renderer.get();
        builder.useBlockId(IrisUtil.getBlockId(blockState));

        FluidState fluidState = blockState.getFluidState();
        if (!fluidState.isEmpty()) renderer.submitFluid(
                origin,
                pos,
                blockAndTintGetter,
                builder,
                blockState,
                fluidState
        );

        //TODO: Block entities (VERY FUN!!!!)

        if (blockState.getRenderShape() == RenderShape.MODEL) {
            renderer.submitBlock(
                    origin,
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
                WorldOrigin origin,
                BlockPos blockPos,
                BlockAndTintGetter blockAndTintGetter,
                VertexBuilder builder,
                BlockState blockState,
                FluidState fluidState
        ) {
            builder.useAtlas(BLOCK_ATLAS);
            builder.setOffset(origin.applyOffset(
                    blockPos.toMutable().sub(
                            blockPos.getX() & 15,
                            blockPos.getY() & 15,
                            blockPos.getZ() & 15
                    )
            ));

            blockRenderer.renderLiquid(blockPos, blockAndTintGetter, (VertexConsumer) builder, blockState, fluidState);
        }

        private void submitBlock(
                WorldOrigin origin,
                BlockPos pos,
                BlockState blockState,
                BlockAndTintGetter blockAndTintGetter,
                VertexBuilder builder
        ) {
            builder.useAtlas(BLOCK_ATLAS);
            builder.setOffset(origin.applyOffset((IBlockPos) pos));

            poseStack.pushPose();

            parts.clear();
            randomSource.setSeed(blockState.getSeed(pos));
            blockRenderer.getBlockModel(blockState).collectParts(randomSource, parts);
            blockRenderer.renderBatched(blockState, pos, blockAndTintGetter, poseStack, (VertexConsumer) builder, false, parts);

            poseStack.popPose();
        }
    }
}
