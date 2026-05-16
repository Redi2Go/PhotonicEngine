package at.redi2go.photonics.core.rendering.world.registry.block;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.BlockModel;
import at.redi2go.photonics.core.rendering.world.registry.block.entry.SimpleBlockEntry;
import at.redi2go.photonics.core.rendering.world.registry.block.template.BlockPartTemplate;
import at.redi2go.photonics.core.rendering.world.registry.objects.WorldObject;
import org.apache.commons.lang3.NotImplementedException;
import org.joml.Vector3i;

import java.util.List;

public class BlockPartImpl implements BlockModel.Part, Disposable {
    private final BlockHeader blockHeader;
    private final BlockPartTemplate partTemplate;

    private BlockModelImpl model;

    public BlockPartImpl(
            BlockHeader weakBlockHeader,
            BlockPartTemplate partTemplate
    ) {
        this.blockHeader = weakBlockHeader;
        this.partTemplate = partTemplate;
    }

    void setModel(BlockModelImpl model) {
        this.model = model;
    }


    void loadDependants(List<WorldObject<?>> output) {
        output.add(blockHeader);
    }

    public int entryData() {
        return blockHeader.entryData();
    }

    @Override
    public Vector3i offset() {
        return partTemplate.offset();
    }

    @Override
    public int boundingVolume() {
        return partTemplate.boundingVolume();
    }

    @Override
    public BlockEntry toEntry(short region) {
        return new SimpleBlockEntry(region, this);
    }

    @Override
    public void close() {
        model.close();
    }
}
