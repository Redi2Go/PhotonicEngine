package at.redi2go.photonics.client.rendering.world.buffer;

public interface MemoryOwner {
   void allocate(MemoryManager var1);

   void free(MemoryManager var1);

   boolean update(MemoryManager var1);

   void afterUpload();

   int getSize();

   MemoryRegion getMemory();
}
