package at.redi2go.photonics.core.rendering.world.registry.block;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.BlockModel;
import at.redi2go.photonics.core.rendering.world.registry.MemoryOwner;
import at.redi2go.photonics.core.rendering.world.registry.block.entry.SimpleBlockEntry;
import at.redi2go.photonics.core.rendering.world.registry.block.template.BlockPartTemplate;
import org.joml.Vector3i;

public class BlockPartImpl implements BlockModel.Part {
    private MemoryOwner.ManagedRef<BlockModelImpl> model;
    private final MemoryOwner.ManagedRef<BlockHeader> header;
    private final MemoryOwner.ManagedRef<BlockPartTemplate> template;

    public BlockPartImpl(
            MemoryOwner.ManagedRef<BlockHeader> header,
            MemoryOwner.ManagedRef<BlockPartTemplate> template
    ) {
        this.header = header;
        this.template = template;
    }

    void setModel(BlockModelImpl model) {
        this.model = model.makeManagedRef();
    }

    public BlockModel.Part acquire() {
        model.acquire();
        header.acquire();
        template.acquire();

        return this;
    }

    public int entryData() {
        return header.get().voxelEntry();
    }

    @Override
    public Vector3i offset() {
        return template.get().offset();
    }

    @Override
    public int boundingVolume() {
        return template.get().boundingVolume();
    }

    @Override
    public BlockEntry toEntry(short region) {
        return new SimpleBlockEntry(region, this);
    }

    @Override
    public void close() {
        model.decrementCount();
        header.decrementCount();
        template.decrementCount();
    }
}
