package at.redi2go.photonics.client.rendering.world.buffer;

import at.redi2go.photonics.client.Photonics;
import at.redi2go.photonics.client.rendering.opengl.objects.GlTarget;
import at.redi2go.photonics.client.rendering.util.BufferUtils;
import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL43;

public class GlMemoryManager implements MemoryManager {
   private int id;
   private final GlTarget target;
   private final boolean staticData;
   private final String name;
   private final ByteBuffer buffer;
   public int index = 0;
   private int uploadBatchSize = Integer.MAX_VALUE;
   private boolean bindingLogged;
   private final Deque<MemoryOwner> uploadQueue = new ConcurrentLinkedDeque<>();
   public final Queue<MemoryRegion> unusedBuffers = new ConcurrentLinkedQueue<>();

   public GlMemoryManager(GlTarget target, String name, int byteSize, boolean staticData) {
      this.target = target;
      this.name = name;
      this.staticData = staticData;
      this.buffer = BufferUtils.createByteBuffer(byteSize);
   }

   private boolean isOpen() {
      return this.id != -1;
   }

   @Override
   public MemoryRegion allocate(int byteSize) {
      Iterator<MemoryRegion> memoryIterator = this.unusedBuffers.iterator();

      while (memoryIterator.hasNext()) {
         MemoryRegion region = memoryIterator.next();
         if (region.end - region.begin == byteSize) {
            memoryIterator.remove();
            region.allocated = true;
            return region;
         }
      }

      int begin = this.index;
      int end = this.index + byteSize;
      if (end > this.buffer.capacity()) {
         throw new OutOfMemoryError("Could not allocate " + byteSize + " bytes");
      }

      this.index += byteSize;
      return new MemoryRegion(this.buffer.slice(begin, end - begin).order(this.buffer.order()), begin, end);
   }

   public MemoryManager allocateRegion(int byteSize) {
      return new GlMemoryManager.BufferRegion(this.allocate(byteSize));
   }

   public void bind(int program, int blockIndex, int bindingPointIndex) {
      this.ensureAllocated();
      GL30.glBindBufferBase(this.target.target, bindingPointIndex, this.id);
      if (this.target == GlTarget.SSBO) {
         GL43.glShaderStorageBlockBinding(program, blockIndex, bindingPointIndex);
      } else if (this.target == GlTarget.UBO) {
         GL31.glUniformBlockBinding(program, blockIndex, bindingPointIndex);
      }

      if (!this.bindingLogged) {
         this.bindingLogged = true;
         Photonics.info(
            "GPU buffer diagnostics: name={}, target={}, program={}, blockIndex={}, bindingPoint={}, buffer={}, capacityBytes={}",
            this.name,
            this.target,
            program,
            blockIndex,
            bindingPointIndex,
            this.id,
            this.buffer.capacity()
         );
      }
   }

   @Nullable
   public GlProgramBuffer findInProgram(int program) {
      this.ensureAllocated();

      int blockIndex = switch (this.target) {
         case SSBO -> GL43.glGetProgramResourceIndex(program, 37606, this.name);
         case UBO -> GL31.glGetUniformBlockIndex(program, this.name);
      };
      return blockIndex == -1 ? null : new GlMemoryManager.FoundBuffer(program, blockIndex);
   }

   @Override
   public boolean upload() {
      this.ensureAllocated();
      GL15.glBindBuffer(this.target.target, this.id);

      for (int i = 0; i < this.uploadBatchSize && !this.uploadQueue.isEmpty(); i++) {
         MemoryOwner memoryOwner = this.uploadQueue.poll();
         if (memoryOwner == null) {
            Photonics.warn("Memory owner is null?");
         } else {
            synchronized (memoryOwner) {
               MemoryRegion memoryRegion = memoryOwner.getMemory();
               if (memoryRegion != null) {
                  GL15.glBufferSubData(this.target.target, memoryRegion.begin, memoryRegion.getBuffer());
                  memoryOwner.afterUpload();
               }
            }
         }
      }

      GL15.glBindBuffer(this.target.target, 0);
      return this.uploadQueue.isEmpty();
   }

   public void download(Consumer<ByteBuffer> downloadContext) {
      this.ensureAllocated();
      GL15.glBindBuffer(this.target.target, this.id);
      ByteBuffer downloadedBuffer = GL15.glMapBuffer(this.target.target, 35002, null);
      downloadContext.accept(downloadedBuffer);
      GL15.glUnmapBuffer(this.target.target);
      GL15.glBindBuffer(this.target.target, 0);
   }

   @Override
   public void queueUpload(MemoryOwner memoryOwner) {
      if (memoryOwner == null) {
         System.err.println("Trying to upload null?");
      } else {
         this.uploadQueue.addLast(memoryOwner);
      }
   }

   @Override
   public void queueUploadPriority(MemoryOwner memoryOwner) {
      if (memoryOwner == null) {
         System.err.println("Trying to upload null?");
      } else {
         this.uploadQueue.addFirst(memoryOwner);
      }
   }

   @Override
   public void free(MemoryRegion memoryRegion) {
      memoryRegion.allocated = false;
      this.unusedBuffers.add(memoryRegion);
   }

   private void ensureAllocated() {
      if (this.id == 0) {
         this.id = GL15.glGenBuffers();
         GL15.glBindBuffer(this.target.target, this.id);
         GL15.glBufferData(this.target.target, this.buffer.capacity(), this.staticData ? 35044 : 35048);
         GL15.glBindBuffer(this.target.target, 0);
      }
   }

   public void setUploadBatchSize(int uploadBatchSize) {
      this.uploadBatchSize = uploadBatchSize;
   }

   @Override
   public int getCapacity() {
      return this.buffer.capacity();
   }

   @Override
   public void free() {
      if (this.id != 0) {
         GL15.glDeleteBuffers(this.id);
         this.id = -1;
      }
   }

   private class BufferRegion implements MemoryManager {
      private final MemoryRegion memory;
      private int index;
      public final Queue<MemoryRegion> unusedBuffers = new ConcurrentLinkedQueue<>();

      public BufferRegion(MemoryRegion region) {
         this.memory = region;
         this.index = region.begin;
      }

      @Override
      public int getCapacity() {
         return this.memory.end - this.memory.begin;
      }

      @Override
      public MemoryRegion allocate(int byteSize) {
         Iterator<MemoryRegion> memoryIterator = this.unusedBuffers.iterator();

         while (memoryIterator.hasNext()) {
            MemoryRegion region = memoryIterator.next();
            if (region.end - region.begin == byteSize) {
               memoryIterator.remove();
               region.allocated = true;
               return region;
            }
         }

         int begin = this.index;
         int end = this.index + byteSize;
         if (end > this.memory.end) {
            throw new OutOfMemoryError("Could not allocate " + byteSize + " bytes");
         }

         this.index += byteSize;
         return new MemoryRegion(GlMemoryManager.this.buffer.slice(begin, end - begin).order(GlMemoryManager.this.buffer.order()), begin, end);
      }

      @Override
      public boolean upload() {
         return GlMemoryManager.this.upload();
      }

      @Override
      public void queueUpload(MemoryOwner memoryOwner) {
         GlMemoryManager.this.queueUpload(memoryOwner);
      }

      @Override
      public void queueUploadPriority(MemoryOwner memoryOwner) {
         GlMemoryManager.this.queueUploadPriority(memoryOwner);
      }

      @Override
      public void free(MemoryRegion memoryRegion) {
         memoryRegion.allocated = false;
         this.unusedBuffers.add(memoryRegion);
      }

      @Override
      public void free() {
         GlMemoryManager.this.free(this.memory);
      }
   }

   private class FoundBuffer implements GlProgramBuffer {
      private final int program;
      private final int blockIndex;

      FoundBuffer(int program, int blockIndex) {
         this.program = program;
         this.blockIndex = blockIndex;
      }

      @Override
      public int blockIndex() {
         return this.blockIndex;
      }

      @Override
      public void bind(int bindingPointIndex) {
         GlMemoryManager.this.bind(this.program, this.blockIndex, bindingPointIndex);
      }
   }
}
