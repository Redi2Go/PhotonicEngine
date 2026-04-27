package at.redi2go.photonics.common.meshing;

import at.redi2go.photonics.api.mc.Id;
import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlockAndTintGetter;
import at.redi2go.photonics.api.mc.world.level.IBlockState;
import at.redi2go.photonics.common.BlockRenderDispatcherExt;
import at.redi2go.photonics.common.iris.IrisUtil;
import at.redi2go.photonics.core.rendering.world.bakery.BlockBuilder;
import at.redi2go.photonics.core.rendering.world.bakery.BlockLod;
import at.redi2go.photonics.core.rendering.world.bakery.BlockMesher;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MinecraftBlockMesher implements BlockMesher {
    private static final ThreadLocal<Renderer> RENDERERS = ThreadLocal.withInitial(Renderer::new);

    // Replace with proper config system
    private @BlockLod int getLod(BlockState blockState) {
        if (blockState.is(BlockTags.LEAVES))
            return BlockLod.NO_SEED | BlockLod.CONTAINED;

        return 0;
    }

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
        var renderer = this.RENDERERS.get();
        int lod = getLod(blockState);
        builder.beginBlock(IrisUtil.getBlockId(blockState), lod, blockChunkOffset);

        FluidState fluidState = blockState.getFluidState();
        if (!fluidState.isEmpty()) renderer.submitFluid(
                pos,
                blockAndTintGetter,
                builder,
                blockState,
                fluidState
        );

        if (blockState.hasBlockEntity()) {
            renderer.submitBlockState(
                    blockState,
                    blockAndTintGetter,
                    builder
            );
        }

        if (blockState.getRenderShape() == RenderShape.MODEL) {
            renderer.submitBlock(
                    lod,
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

        private final BlockBuilderBufferSource bufferSource = new BlockBuilderBufferSource();

        private final LevelRenderState levelRenderState = new LevelRenderState();
        private final SubmitNodeStorage submitNodeStorage = new SubmitNodeStorage();
        private final FeatureRenderDispatcher featureRenderDispatcher = new FeatureRenderDispatcher(
                submitNodeStorage,
                blockRenderer,
                bufferSource,
                Minecraft.getInstance().getAtlasManager(),
                EmptyOutlineBufferSource.INSTANCE,
                EmptyBufferSource.INSTANCE,
                Minecraft.getInstance().font
        );

        public Renderer() {
            var featureRenderer = (FeatureRendererExt) featureRenderDispatcher;

            featureRenderer.setRenderShadows(false);
            featureRenderer.setRenderFlames(false);
            featureRenderer.setRenderNametags(false);
            featureRenderer.setRenderText(false);
            featureRenderer.setRenderParticles(false);
        }

        private static final Id BLOCK_ATLAS = (Id) (Object) TextureAtlas.LOCATION_BLOCKS;

        private static final Set<Fluid> WHITELISTED_FLUIDS = Set.of(Fluids.LAVA, Fluids.FLOWING_LAVA);

        private void submitFluid(
                BlockPos blockPos,
                BlockAndTintGetter blockAndTintGetter,
                BlockBuilder builder,
                BlockState blockState,
                FluidState fluidState
        ) {
            if (!WHITELISTED_FLUIDS.contains(fluidState.getType())) return;

            builder.useAtlas(BLOCK_ATLAS);
            builder.useOffset(
                    -(blockPos.getX() & 15),
                    -(blockPos.getY() & 15),
                    -(blockPos.getZ() & 15)
            );

            blockRenderer.renderLiquid(blockPos, blockAndTintGetter, (VertexConsumer) builder, blockState, fluidState);
        }

        private void submitBlock(
                int lod,
                BlockPos pos,
                BlockState blockState,
                BlockAndTintGetter blockAndTintGetter,
                BlockBuilder builder
        ) {
            builder.useAtlas(BLOCK_ATLAS);
            builder.useOffset(0f, 0f, 0f);

            poseStack.pushPose();

            parts.clear();
            randomSource.setSeed((lod & BlockLod.NO_SEED) == 0 ? blockState.getSeed(pos) : 0);
            blockRenderer.getBlockModel(blockState).collectParts(randomSource, parts);
            ((BlockRenderDispatcherExt) blockRenderer)
                    .photonics$modelBlockRenderer()
                    .tesselateWithoutAO(blockAndTintGetter, parts, blockState, pos, poseStack, (VertexConsumer) builder, false, OverlayTexture.NO_OVERLAY);

            poseStack.popPose();
        }

        private static final Set<Block> LEVEL_REQUIRED_FOR = Set.of(
                Blocks.CHEST
        );

        private void submitBlockState(
                BlockState blockState,
                BlockAndTintGetter blockAndTintGetter,
                BlockBuilder builder
        ) {
            builder.useOffset(0f, 0f, 0f);
            bufferSource.setBlockBuilder(builder);

            levelRenderState.reset();

            try {
                EntityBlock entityBlock = (EntityBlock) blockState.getBlock();
                BlockEntity entity = entityBlock.newBlockEntity(new BlockPos(0, 0, 0), blockState);
                if (entity == null) return;

                if (LEVEL_REQUIRED_FOR.contains(blockState.getBlock()))
                    entity.setLevel((Level) blockAndTintGetter);

                BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer =
                        Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(entity);

                if (renderer == null) return;

                var renderState = renderer.createRenderState();
                renderer.extractRenderState(entity, renderState, 0.8f, levelRenderState.cameraRenderState.pos, null);

                poseStack.pushPose();
                renderer.submit(renderState, poseStack, submitNodeStorage, levelRenderState.cameraRenderState);
                poseStack.popPose();

                featureRenderDispatcher.renderAllFeatures();
            } finally {
                bufferSource.setBlockBuilder(null);
            }
        }
    }
}
