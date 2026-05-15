package at.redi2go.photonics.core.rendering.world.tree;

public interface ChunkManager {
    void addChunk(ChunkVoxel chunk);

    void removeChunk(ChunkVoxel chunk);
}
