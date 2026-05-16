package at.redi2go.photonics.core.rendering.world.registry.block;

import at.redi2go.photonics.core.rendering.world.bakery.BlockMeshState;
import at.redi2go.photonics.core.rendering.world.block.BlockModel;
import at.redi2go.photonics.core.rendering.world.registry.ManagedObject;
import at.redi2go.photonics.core.rendering.world.registry.WorldRegistry;
import at.redi2go.photonics.core.rendering.world.registry.block.template.BlockModelTemplate;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlockModelImpl extends ManagedObject<BlockModelImpl> implements BlockModel {
    private final ManagedRef<BlockModelTemplate> template;
    private final long hash;
    private final PartsWrapper parts;

    private final List<BlockMeshState> meshes = new ArrayList<>();

    public BlockModelImpl(
            WorldRegistry registry,
            ManagedRef<BlockModelTemplate> template,
            long hash,
            List<BlockPartImpl> parts
    ) {
        super(registry);

        this.template = template;
        this.hash = hash;
        this.parts = new PartsWrapper(parts);

        parts.forEach(e -> e.setModel(this));

        acquireDependants();
    }

    @Override
    protected void loadDependants(List<ManagedRef<?>> output) {
        output.add(template);
    }

    @Override
    protected BlockModelImpl getWrappedValue() {
        return this;
    }

    public void addMeshState(BlockMeshState meshState) {
        meshes.add(meshState);
    }

    @Override
    protected boolean dispose() {
        if (!super.dispose()) return false;

        template.get().removeVariant(hash);
        meshes.forEach(registry::removeBlockModel);

        return true;
    }

    @Override
    public List<Part> parts() {
        return parts;
    }

    private class PartsWrapper extends AbstractList<BlockModel.Part> {
        private final List<BlockPartImpl> parts;

        private PartsWrapper(List<BlockPartImpl> parts) {
            this.parts = parts;
        }

        @Override
        public int size() {
            return parts.size();
        }

        @Override
        public BlockModel.Part get(int index) {
            Objects.checkIndex(index, parts.size());
            return parts.get(index).acquire();
        }
    }
}
