package at.redi2go.photonics.client.rendering.world.buffer;

public class SimpleMemoryOwner implements MemoryOwner {
   private MemoryRegion memory;
   private final int size;

   public SimpleMemoryOwner(GlMemoryManager memoryManager, int size) {
      this.size = size;
      this.allocate(memoryManager);
   }

   public int[] test() {
      int[] test = new int[this.getSize() >> 2];
      this.getMemory().getBuffer().asIntBuffer().get(test);

      for (int i = 0; i < test.length; i++) {
         if (test[i] != 0) {
            System.out.println("found " + test[i] + " at " + i);
         }
      }

      return test;
   }

   @Override
   public void allocate(MemoryManager memoryManager) {
      if (this.memory != null) {
         this.free(memoryManager);
      }

      this.memory = memoryManager.allocate(this.size);
   }

   @Override
   public void free(MemoryManager memoryManager) {
      memoryManager.free(this.memory);
      this.memory = null;
   }

   @Override
   public boolean update(MemoryManager memoryManager) {
      return false;
   }

   @Override
   public void afterUpload() {
   }

   @Override
   public int getSize() {
      return this.size;
   }

   @Override
   public MemoryRegion getMemory() {
      return this.memory;
   }
}
