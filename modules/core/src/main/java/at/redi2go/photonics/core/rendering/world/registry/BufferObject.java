package at.redi2go.photonics.core.rendering.world.registry;

public interface BufferObject extends ManagedObject {
    boolean isAllocated();

    default void awaitAllocated() {
        while (!isAllocated())
            Thread.onSpinWait();
    }

    int begin();
}
