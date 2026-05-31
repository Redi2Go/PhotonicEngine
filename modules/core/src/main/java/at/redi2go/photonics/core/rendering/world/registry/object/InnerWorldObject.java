package at.redi2go.photonics.core.rendering.world.registry.object;

import at.redi2go.photonics.api.Disposable;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class InnerWorldObject<M extends Disposable> extends AbstractWorldObject<M> {
    public InnerWorldObject(ObjectRegistry<?> registry) {
        super(registry);
    }

    public M setMemory(Supplier<M> memorySupplier) {
        return super.setMemory(memorySupplier);
    }

    public @Nullable M memoryOrNull() {
        return super.memoryOrNull();
    }

    public M memoryOrThrow() {
        return super.memoryOrThrow();
    }
}
