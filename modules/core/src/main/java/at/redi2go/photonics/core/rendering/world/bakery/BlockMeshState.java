package at.redi2go.photonics.core.rendering.world.bakery;

public interface BlockMeshState {
    boolean shouldCache();

    void prepareCacheUse();
}
