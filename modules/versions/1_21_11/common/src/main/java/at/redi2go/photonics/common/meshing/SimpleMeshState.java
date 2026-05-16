package at.redi2go.photonics.common.meshing;

import at.redi2go.photonics.core.rendering.world.block.VoxelColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

public class SimpleMeshState implements McMeshState {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final BlockColors BLOCK_COLORS = Minecraft.getInstance().getBlockColors();

    private final Block block;
    private final int blockId;
    private List<BlockModelPart> blockModel;

    private long hashCode = 0;

    public SimpleMeshState(
            Block block,
            int blockId,
            List<BlockModelPart> blockModel
    ) {
        this.block = block;
        this.blockId = blockId;
        this.blockModel = blockModel;
    }

    public void computeHash(
            HashStorage hashStorage,
            BlockState blockState,
            BlockPos blockPos,
            BlockAndTintGetter blockAndTintGetter
    ) {
        hashStorage.lastTintIndex = Integer.MIN_VALUE;
        hashStorage.lastTint = VoxelColor.WHITE;

        long hashCode = blockId;

        for (var part : blockModel) {
            for (var direction : DIRECTIONS) {
                hashCode = hashCode * 31 + hashPart(
                        hashStorage,
                        blockState,
                        blockPos,
                        blockAndTintGetter,
                        part.getQuads(direction)
                );
            }

            hashCode = hashCode * 31 + hashPart(
                    hashStorage,
                    blockState,
                    blockPos,
                    blockAndTintGetter,
                    part.getQuads(null)
            );
        }

        this.hashCode = hashCode;
    }

    private static long hashPart(
            HashStorage hashStorage,
            BlockState blockState,
            BlockPos blockPos,
            BlockAndTintGetter blockAndTintGetter,
            List<BakedQuad> quads
    ) {
        long hash = 1;

        for (var quad : quads) {
            hash = hash * 31 + hashQuad(
                    hashStorage,
                    blockState,
                    blockPos,
                    blockAndTintGetter,
                    quad
            );
        }

        return hash;
    }

    private static long hashQuad(
            HashStorage hashStorage,
            BlockState blockState,
            BlockPos blockPos,
            BlockAndTintGetter blockAndTintGetter,
            BakedQuad bakedQuad
    ) {
        long hash;

        int tintIndex = bakedQuad.tintIndex();
        if (tintIndex != -1) {
            if (hashStorage.lastTintIndex == tintIndex) {
                hash = hashStorage.lastTint;
            } else {
                int tintColor = BLOCK_COLORS.getColor(blockState, blockAndTintGetter, blockPos, tintIndex);

                hashStorage.lastTintIndex = tintIndex;
                hashStorage.lastTint = tintColor;
                hash = tintColor;
            }
        } else hash = VoxelColor.WHITE;

        hash = hash * 31 + bakedQuad.position0().hashCode();
        hash = hash * 31 + bakedQuad.position1().hashCode();
        hash = hash * 31 + bakedQuad.position2().hashCode();
        hash = hash * 31 + bakedQuad.position3().hashCode();

        hash = hash * 31 + bakedQuad.packedUV0();
        hash = hash * 31 + bakedQuad.packedUV1();
        hash = hash * 31 + bakedQuad.packedUV2();
        hash = hash * 31 + bakedQuad.packedUV3();

        return hash;
    }

    @Override
    public boolean shouldCache() {
        return true;
    }

    @Override
    public void prepareCacheUse() {
        blockModel = List.of();
    }

    @Override
    public int blockId() {
        return blockId;
    }

    @Override
    public FluidState fluidState() {
        return Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public List<BlockModelPart> blockModel() {
        return blockModel;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(hashCode);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SimpleMeshState other && other.hashCode == hashCode && other.block == block && other.blockId == blockId();
    }

    public static class HashStorage {
        private int lastTintIndex = -1;
        private int lastTint = VoxelColor.WHITE;
    }
}
