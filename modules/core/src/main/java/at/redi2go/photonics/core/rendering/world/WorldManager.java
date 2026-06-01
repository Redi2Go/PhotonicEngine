package at.redi2go.photonics.core.rendering.world;

import at.redi2go.photonics.core.rendering.world.tree.nodes.ChunkNode;

public interface WorldManager {
    void addChunk(ChunkNode chunk);

    void removeChunk(ChunkNode chunk);

    void queueUpload(int depth, Runnable job);
}
