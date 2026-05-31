package at.redi2go.photonics.core.rendering.world.registry.block.model;

import at.redi2go.photonics.core.rendering.world.bakery.BlockMeshState;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.BlockModel;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockLayer;
import at.redi2go.photonics.core.rendering.world.registry.object.AbstractWorldObject;
import at.redi2go.photonics.core.rendering.world.registry.object.NoMemory;
import at.redi2go.photonics.core.rendering.world.registry.object.WorldObject;
import org.joml.Vector3i;

import java.util.AbstractList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockModelImpl extends AbstractWorldObject<NoMemory> implements BlockModel {
    private final long vertexHash;
    private final PartsWrapper parts;

    private final Queue<BlockMeshState> meshes = new ConcurrentLinkedQueue<>();

    public BlockModelImpl(
            long vertexHash,
            List<BlockPartImpl> parts,
            BlockModelRegistry registry
    ) {
        super(registry);

        this.vertexHash = vertexHash;
        this.parts = new PartsWrapper(parts);

        parts.forEach(e -> e.owner = this);

        setMemory(() -> NoMemory.INSTANCE);
    }

    public long vertexHash() {
        return vertexHash;
    }

    public Queue<BlockMeshState> meshes() {
        return meshes;
    }

    @Override
    protected void loadDependants(List<WorldObject> output) {
        for (var part : parts.backing)
            output.add(part.blockLayer());
    }

    @Override
    public List<Part> parts() {
        return parts;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(vertexHash);
    }

    @Override
    public boolean equals(Object o) {
        return o == this || (o instanceof BlockModelImpl model && model.vertexHash == vertexHash);
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
