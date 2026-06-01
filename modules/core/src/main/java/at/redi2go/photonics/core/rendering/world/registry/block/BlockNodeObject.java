package at.redi2go.photonics.core.rendering.world.registry.block;

import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryListMemory;
import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryMemory;
import at.redi2go.photonics.core.rendering.world.allocator.WorldAllocator;
import at.redi2go.photonics.core.rendering.world.registry.object.InnerWorldObject;
import at.redi2go.photonics.core.rendering.world.registry.object.WorldObject;
import at.redi2go.photonics.core.rendering.world.registry.palete.PaletteObject;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeNode;

import java.util.List;

public abstract class BlockNodeObject extends VoxelTreeNode implements BlockObject {
    private final Reference ref;
    private long childMask = 0;

    public BlockNodeObject(BlockRegistry blockRegistry, int depth) {
        super(depth);
        this.ref = new Reference(blockRegistry);
    }

    protected abstract boolean useChildMask();

    protected abstract int extraFieldCount();

    @Override
    public void allocate(WorldAllocator allocator) {
        var memory = ref.setMemory(() -> allocator.allocateEntryList(useChildMask(), extraFieldCount()));
        this.childMask = writeEntries(memory);

        memory.upload();
    }

    @Override
    public void uploadTo(VoxelEntryMemory memory) {
        awaitAllocated();

        memory.setEntryData(ref.memoryOrThrow().entryData());
        memory.setChildMask(childMask);
    }

    protected void loadDependants(List<WorldObject> output) {
        for (int i = 0; i < ENTRIES_SIZE; i++) {
            var entry = data[i];
            if (entry == null) continue;

            output.add((WorldObject) entry);
        }
    }


    @Override
    public boolean isAllocated() {
        return ref.isAllocated();
    }

    @Override
    public void awaitAllocated() {
        ref.awaitAllocated();
    }

    @Override
    public void acquireReference() {
        ref.acquireReference();
    }

    @Override
    public boolean tryAcquireReference() {
        return ref.tryAcquireReference();
    }

    @Override
    public void close() {
        ref.close();
    }

    private class Reference extends InnerWorldObject<VoxelEntryListMemory> {
        public Reference(BlockRegistry registry) {
            super(registry);
        }

        @Override
        protected void loadDependants(List<WorldObject> output) {
            BlockNodeObject.this.loadDependants(output);
        }
    }
}
