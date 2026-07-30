package at.redi2go.photonics.client.rendering.world.buffer;

import at.redi2go.photonics.client.rendering.opengl.objects.Destructable;

public interface MemoryManager extends Destructable {
   int getCapacity();

   MemoryRegion allocate(int var1);

   boolean upload();

   void queueUpload(MemoryOwner var1);

   void queueUploadPriority(MemoryOwner var1);

   void free(MemoryRegion var1);
}
