package at.redi2go.photonics.core.rendering.world.registry.object;

import at.redi2go.photonics.api.Disposable;

public class NoMemory implements Disposable {
    public static final NoMemory INSTANCE = new NoMemory();

    private NoMemory() {

    }

    @Override
    public void close() {

    }
}
