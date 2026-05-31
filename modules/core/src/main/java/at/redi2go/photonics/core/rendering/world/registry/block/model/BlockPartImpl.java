package at.redi2go.photonics.core.rendering.world.registry.block.model;

import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.BlockModel;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockLayer;
import at.redi2go.photonics.core.rendering.world.registry.block.entry.SimpleBlockEntry;
import org.apache.commons.lang3.NotImplementedException;
import org.joml.Vector3i;

public class BlockPartImpl implements BlockModel.Part {
    private final Vector3i offset;
    private final BlockLayer block;

    BlockModelImpl owner;

    public BlockPartImpl(Vector3i offset, BlockLayer block) {
        this.offset = offset;
        this.block = block;
    }

    @Override
    public Vector3i offset() {
        return offset;
    }

    public BlockLayer blockLayer() {
        return block;
    }

    @Override
    public BlockEntry createEntry(int region) {
        return new SimpleBlockEntry(region, this);
    }

    @Override
    public void close() {
        owner.close();
    }
}
