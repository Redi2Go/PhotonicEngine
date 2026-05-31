package at.redi2go.photonics.common.meshing;

import at.redi2go.photonics.core.rendering.world.bakery.BlockMeshState;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.world.level.material.FluidState;

import java.util.List;

public interface McMeshState extends BlockMeshState {
    int blockId();

    FluidState fluidState();

    List<BlockModelPart> blockModel();
}
