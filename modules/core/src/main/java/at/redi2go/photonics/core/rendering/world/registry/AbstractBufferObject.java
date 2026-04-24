package at.redi2go.photonics.core.rendering.world.registry;

import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;

public abstract class AbstractBufferObject extends AbstractManagedObject implements BufferObject {
    protected MemoryView memory;

    protected AbstractBufferObject(BufferBlockRegistry registry) {
        super(registry);
    }

    @Override
    public boolean isAllocated() {
        return memory != null;
    }

    @Override
    public int begin() {
        awaitAllocated();
        return MemoryView.intBufferBegin(memory);
    }

    protected void initMemory(int byteSize) {
        memory = registry.allocate(byteSize);
        isOpen = true;
    }

    @Override
    protected void dispose() {
        memory.close();
    }
}
