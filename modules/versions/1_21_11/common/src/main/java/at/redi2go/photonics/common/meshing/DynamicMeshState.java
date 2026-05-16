package at.redi2go.photonics.common.meshing;

import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.world.level.material.FluidState;

import java.util.List;

public record DynamicMeshState(
        int blockId,
        FluidState fluidState,
        List<BlockModelPart> blockModel
) implements McMeshState {
    @Override
    public boolean shouldCache() {
        return false;
    }

    @Override
    public void prepareCacheUse() {

    }
}
