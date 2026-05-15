package at.redi2go.photonics.core.rendering.world.registry.block;

import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.BlockModel;
import at.redi2go.photonics.core.rendering.world.registry.MemoryOwner;
import at.redi2go.photonics.core.rendering.world.registry.block.entry.SimpleBlockEntry;
import at.redi2go.photonics.core.rendering.world.registry.block.template.BlockPartTemplate;
import org.apache.commons.lang3.NotImplementedException;
import org.joml.Vector3i;

public class BlockPartOwner {
    private final MemoryOwner.ManagedRef<BlockHeader> header;
    private final MemoryOwner.ManagedRef<BlockPartTemplate> template;

    public BlockPartOwner(
            MemoryOwner.ManagedRef<BlockHeader> header,
            MemoryOwner.ManagedRef<BlockPartTemplate> template
    ) {
        this.header = header;
        this.template = template;
    }

    public BlockModel.Part makePart(MemoryOwner.ManagedRef<BlockModelImpl> model) {
        return new PartImpl(
                model.elevate(),
                header.elevate(),
                template.elevate()
        );
    }

    public record PartImpl(
            MemoryOwner.Ref<BlockModelImpl> model,
            MemoryOwner.Ref<BlockHeader> header,
            MemoryOwner.Ref<BlockPartTemplate> template
    ) implements BlockModel.Part {
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
            model.close();
            header.close();
            template.close();
        }
    }
}
