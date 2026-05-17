package at.redi2go.photonics.common.meshing;

import at.redi2go.photonics.api.mc.Id;
import at.redi2go.photonics.common.BlockRenderDispatcherExt;
import at.redi2go.photonics.common.iris.IrisUtil;
import at.redi2go.photonics.common.meshing.impl.BlockBuilderBufferSource;
import at.redi2go.photonics.common.meshing.impl.BlockSetBuilder;
import at.redi2go.photonics.common.meshing.impl.EmptyBufferSource;
import at.redi2go.photonics.common.meshing.impl.EmptyOutlineBufferSource;
import at.redi2go.photonics.common.meshing.impl.FeatureRendererExt;
import at.redi2go.photonics.core.rendering.world.bakery.BlockBuilder;
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

public class McBlockRenderer {
    private static final Id BLOCK_ATLAS = (Id) (Object) TextureAtlas.LOCATION_BLOCKS;
    private static final Set<Fluid> WHITELISTED_FLUIDS = Set.of(Fluids.LAVA, Fluids.FLOWING_LAVA);

    private final RandomSource randomSource = RandomSource.create();
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

    private final SimpleMeshState.HashStorage hashStorage = new SimpleMeshState.HashStorage();

    public McBlockRenderer() {
        var featureRenderer = (FeatureRendererExt) featureRenderDispatcher;

        featureRenderer.setRenderShadows(false);
        featureRenderer.setRenderFlames(false);
        featureRenderer.setRenderNametags(false);
        featureRenderer.setRenderText(false);
        featureRenderer.setRenderParticles(false);
    }

    public McMeshState extractMeshState(
            Vector3i blockChunkOffset,
            BlockPos blockPos,
            BlockState blockState,
            BlockAndTintGetter blockAndTintGetter
    ) {
        int blockId = IrisUtil.getBlockId(blockState);
        FluidState fluidState = blockState.getFluidState();

        List<BlockModelPart> parts;
        if (blockState.getRenderShape() == RenderShape.MODEL) {
            parts = new ArrayList<>();

            randomSource.setSeed(blockState.getSeed(blockPos));
            blockRenderer.getBlockModel(blockState).collectParts(randomSource, parts);
        } else parts = List.of();

        if (blockState.hasBlockEntity()) return new DynamicMeshState(blockId, fluidState, parts);
        if (WHITELISTED_FLUIDS.contains(fluidState.getType())) return new DynamicMeshState(blockId, fluidState, parts);

        if (parts.isEmpty()) return EmptyMeshState.INSTANCE;

        var meshState = new SimpleMeshState(blockState.getBlock(), blockId, parts);
        meshState.computeHash(
                hashStorage,
                blockState,
                blockPos,
                blockAndTintGetter
        );

        return meshState;
    }

    public void meshBlock(
            McMeshState meshState,
            Vector3i blockChunkOffset,
            BlockPos pos,
            BlockState blockState,
            BlockAndTintGetter blockAndTintGetter,
            BlockBuilder builder
    ) {
        if (meshState == EmptyMeshState.INSTANCE) return;

        builder.useBlockId(meshState.blockId());

        FluidState fluidState = meshState.fluidState();
        if (!fluidState.isEmpty()) {
            submitFluid(
                    pos,
                    blockAndTintGetter,
                    builder,
                    blockState,
                    fluidState
            );
        }

        if (blockState.hasBlockEntity()) {
            submitBlockEntity(
                    pos,
                    blockState,
                    blockAndTintGetter,
                    builder
            );
        }

        List<BlockModelPart> parts = meshState.blockModel();
        if (!parts.isEmpty()) {
            submitBlock(
                    pos,
                    blockState,
                    blockAndTintGetter,
                    builder,
                    parts
            );
        }
    }

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
            BlockPos pos,
            BlockState blockState,
            BlockAndTintGetter blockAndTintGetter,
            BlockBuilder builder,
            List<BlockModelPart> parts
    ) {
        builder.useAtlas(BLOCK_ATLAS);
        builder.useOffset(0f, 0f, 0f);

        poseStack.pushPose();

        ((BlockRenderDispatcherExt) blockRenderer)
                .photonics$modelBlockRenderer()
                .tesselateWithoutAO(
                        blockAndTintGetter,
                        parts,
                        blockState,
                        pos,
                        poseStack,
                        (VertexConsumer) builder,
                        false,
                        OverlayTexture.NO_OVERLAY
                );

        poseStack.popPose();
    }

    private static final Set<Block> FULL_BLOCK_ENTITY_REQUIRED_FOR = new BlockSetBuilder()
            .addBlock(Blocks.PLAYER_HEAD)
            .addBlock(Blocks.PLAYER_WALL_HEAD)
            .build();

    private static final Set<Block> LEVEL_REQUIRED_FOR = new BlockSetBuilder()
            .addBlock(Blocks.CHEST)
            .build();

    private BlockEntity copyBlockEntity(BlockState blockState) {
        EntityBlock entityBlock = (EntityBlock) blockState.getBlock();
        return entityBlock.newBlockEntity(new BlockPos(0, 0, 0), blockState);
    }

    private BlockEntity fetchBlockEntity(BlockPos blockPos, BlockState blockState) {
        try {
            var level = Minecraft.getInstance().level;
            if (level == null || !level.isInValidBounds(blockPos)) return copyBlockEntity(blockState);

            var chunk = level.getChunkAt(blockPos);
            return chunk.getBlockEntity(blockPos);
        } catch (Exception e) {
            return copyBlockEntity(blockState);
        }
    }

    private void submitBlockEntity(
            BlockPos blockPos,
            BlockState blockState,
            BlockAndTintGetter blockAndTintGetter,
            BlockBuilder builder
    ) {
        builder.useOffset(0f, 0f, 0f);
        bufferSource.setBlockBuilder(builder);

        levelRenderState.reset();

        try {
            BlockEntity entity = FULL_BLOCK_ENTITY_REQUIRED_FOR.contains(blockState.getBlock()) ?
                    fetchBlockEntity(blockPos, blockState) :
                    copyBlockEntity(blockState);

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
