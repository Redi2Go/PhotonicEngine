package at.redi2go.photonics.common.meshing;

import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

public class EmptyMeshState implements McMeshState {
    public static final EmptyMeshState INSTANCE = new EmptyMeshState();

    private EmptyMeshState() {

    }

    @Override
    public int blockId() {
        return -1;
    }

    @Override
    public FluidState fluidState() {
        return Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public List<BlockModelPart> blockModel() {
        return List.of();
    }

    @Override
    public boolean shouldCache() {
        return true;
    }

    @Override
    public void prepareCacheUse() {

    }
}
