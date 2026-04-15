package at.redi2go.photonics.core.rendering.world.allocator;

import at.redi2go.photonics.api.Disposable;

public record TempMapping(BufferWorldAllocator allocator, int id) implements Disposable {
    @Override
    public void close() {
        allocator.removeTempMapping(id);
    }
}
