package at.redi2go.photonics.core.rendering.world.registry.block;

import at.redi2go.photonics.core.rendering.world.bakery.BlockMeshState;
import at.redi2go.photonics.core.rendering.world.block.BlockModel;
import at.redi2go.photonics.core.rendering.world.registry.WorldRegistry;
import at.redi2go.photonics.core.rendering.world.registry.block.template.BlockModelTemplate;
import at.redi2go.photonics.core.rendering.world.registry.objects.NoMemory;
import at.redi2go.photonics.core.rendering.world.registry.objects.WorldObject;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockModelImpl extends WorldObject<NoMemory> implements BlockModel {
    private final BlockModelTemplate template;
    private final long hash;
    private final PartsWrapper parts;

    private final Queue<BlockMeshState> meshes = new ConcurrentLinkedQueue<>();

    public BlockModelImpl(
            WorldRegistry worldRegistry,
            BlockModelTemplate weakTemplate,
            long hash,
            List<BlockPartImpl> parts
    ) {
        super(worldRegistry);

        this.template = weakTemplate;
        this.hash = hash;

        this.parts = new PartsWrapper(parts);
        parts.forEach(e -> e.setModel(this));

        setMemory(() -> NoMemory.INSTANCE);
    }

    @Override
    protected void loadDependants(List<WorldObject<?>> output) {
        output.add(template);

        for (var part : parts.backing)
            part.loadDependants(output);
    }

    @Override
    public List<Part> parts() {
        return parts;
    }

    public void addMeshState(BlockMeshState meshState) {
        meshes.add(meshState);
    }

    @Override
    protected boolean dispose() {
        if (!super.dispose()) return false;

        template.removeVariant(hash);
        while (!meshes.isEmpty()) {
            var mesh = meshes.remove();
            if (mesh == null) break;

            worldRegistry.removeBlockModel(mesh);
        }

        return true;
    }

    private class PartsWrapper extends AbstractList<BlockModel.Part> {
        private final List<BlockPartImpl> backing;

        private PartsWrapper(List<BlockPartImpl> parts) {
            this.backing = parts;
        }

        @Override
        public int size() {
            return backing.size();
        }

        @Override
        public BlockModel.Part get(int index) {
            Objects.checkIndex(index, backing.size());
            var result = backing.get(index);
            BlockModelImpl.this.acquireReference();

            return result;
        }
    }
}
