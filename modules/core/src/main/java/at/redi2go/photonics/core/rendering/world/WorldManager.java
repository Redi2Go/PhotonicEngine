package at.redi2go.photonics.core.rendering.world;

public interface WorldManager {
    void queueUpload(int depth, Runnable job);
}
