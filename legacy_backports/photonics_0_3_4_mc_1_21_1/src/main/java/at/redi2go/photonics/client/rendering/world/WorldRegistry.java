package at.redi2go.photonics.client.rendering.world;

import at.redi2go.photonics.client.rendering.MinecraftAccessor;
import at.redi2go.photonics.client.rendering.opengl.objects.Destructable;
import at.redi2go.photonics.client.rendering.opengl.objects.GlTarget;
import at.redi2go.photonics.client.rendering.opengl.rendering.IRenderDispatcher;
import at.redi2go.photonics.client.rendering.schematics.Schematic;
import at.redi2go.photonics.client.rendering.world.buffer.GlMemoryManager;
import at.redi2go.photonics.client.rendering.world.buffer.MemoryManager;
import at.redi2go.photonics.client.rendering.world.buffer.MemoryOwner;
import at.redi2go.photonics.client.rendering.world.buffer.MemoryRegion;
import at.redi2go.photonics.client.rendering.world.position.PBlockPos;
import at.redi2go.photonics.client.rendering.world.position.PChunkPos;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class WorldRegistry implements MemoryOwner, Destructable {
   public static final int MAX_BLOCKS = 8191;
   private final IRenderDispatcher renderDispatcher;
   private final boolean blockLightEnabled;
   private final GlMemoryManager cbMemoryManager;
   private final GlMemoryManager rootMemoryManager;
   private final LightRegistry lightRegistry;
   private final BlockRegistry blockRegistry;
   private final WorldCompilerThread worldCompilerThread;
   private MemoryRegion rootMemory;
   private final Schematic rootSchematic;
   private final ConcurrentLinkedQueue<Runnable> buildQueue = new ConcurrentLinkedQueue<>();
   private final ConcurrentLinkedQueue<Runnable> glQueue = new ConcurrentLinkedQueue<>();
   private final Map<PChunkPos, PChunk> chunks = new HashMap<>();
   private PBlockPos rtToWorldBlockOffset = new PBlockPos(0, 0, 0);
   private PChunkPos rtToWorldChunkOffset = new PChunkPos(0, 0, 0);
   private Vector3d liveWorldBlockOffset = new Vector3d();
   private PBlockPos liveWorldMinVoxel = new PBlockPos(0, 0, 0);
   private PBlockPos liveWorldMaxVoxel = new PBlockPos(0, 0, 0);
   private WorldRegistry.BuildStage buildStage = WorldRegistry.BuildStage.IDLE;
   private boolean closeChunkUpdate = false;
   private boolean closeChunkUpload = false;
   private final int worldBlockSize;
   private final int worldChunkSize;
   private PBlockPos worldMinVoxel = new PBlockPos(0, 0, 0);
   private PBlockPos worldMaxVoxel = new PBlockPos(0, 0, 0);
   private boolean dirty = true;
   private int firstBuildTime = 0;
   private int lastBuildTime = 0;
   private static final Direction[] FACES = new Direction[6];

   public WorldRegistry(IRenderDispatcher renderDispatcher, int maxLights, int maxLightsPerNode, boolean blockLightEnabled, boolean lightBinningEnabled) {
      this.renderDispatcher = renderDispatcher;
      this.blockLightEnabled = blockLightEnabled;
      this.worldChunkSize = 32;
      this.worldBlockSize = 16 * this.worldChunkSize;
      this.rootSchematic = new Schematic(this.worldChunkSize, this.worldChunkSize, this.worldChunkSize);
      this.lightRegistry = new LightRegistry(maxLights, maxLightsPerNode, 8, this.worldBlockSize, renderDispatcher::isChunkEmpty, lightBinningEnabled);
      this.cbMemoryManager = new GlMemoryManager(GlTarget.SSBO, "cb_block", 536870912, true);
      this.blockRegistry = new BlockRegistry(this.cbMemoryManager.allocateRegion(8192 * PBlock.BYTE_SIZE));
      this.rootMemoryManager = new GlMemoryManager(GlTarget.SSBO, "root_uniform", 4 * this.worldChunkSize * this.worldChunkSize * this.worldChunkSize, false);
      this.worldCompilerThread = new WorldCompilerThread(this);
      this.allocate(this.rootMemoryManager);
   }

   public void upload() {
      if (this.buildStage != WorldRegistry.BuildStage.WAIT_FOR_UPLOAD) {
         this.lightRegistry.clearLightMappings();
      } else {
         this.changeBuildStage(WorldRegistry.BuildStage.UPLOAD);
         boolean uploadDone = true;
         uploadDone &= this.blockRegistry.upload();
         uploadDone &= this.lightRegistry.upload();
         uploadDone &= this.cbMemoryManager.upload();
         if (!uploadDone) {
            this.buildStage = WorldRegistry.BuildStage.WAIT_FOR_UPLOAD;
         } else {
            this.rootMemoryManager.upload();
            this.liveWorldBlockOffset = new Vector3d(this.rtToWorldBlockOffset.x, this.rtToWorldBlockOffset.y, this.rtToWorldBlockOffset.z);
            this.liveWorldMinVoxel = this.worldMinVoxel;
            this.liveWorldMaxVoxel = this.worldMaxVoxel;
            if (this.closeChunkUpdate) {
               this.renderDispatcher.onChunkLoad();
               this.closeChunkUpload = true;
               this.closeChunkUpdate = false;
            }

            this.lastBuildTime = SystemTimeUniforms.COUNTER.getAsInt();
            if (this.firstBuildTime == 0) {
               this.firstBuildTime = this.lastBuildTime;
            }

            this.changeBuildStage(WorldRegistry.BuildStage.IDLE);
            if (!this.buildQueue.isEmpty()) {
               this.wakeUpWorldBuilder();
            }
         }
      }
   }

   public void compileWorld() {
      if (this.buildStage == WorldRegistry.BuildStage.IDLE) {
         this.ensureWorldThread();
         this.changeBuildStage(WorldRegistry.BuildStage.COMPILE);
         PBlockPos previousBlockOffset = this.rtToWorldBlockOffset;

         while (!this.buildQueue.isEmpty()) {
            this.buildQueue.poll().run();
         }

         Vector3d pos = MinecraftAccessor.getCameraPosition();
         Vector3f worldOffset = new Vector3f((float)pos.x, (float)pos.y, (float)pos.z);
         worldOffset.floor();
         worldOffset.add(-this.worldBlockSize / 2.0F, -this.worldBlockSize / 2.0F, -this.worldBlockSize / 2.0F);
         worldOffset.mul(0.0625F);
         worldOffset.floor();
         this.rtToWorldChunkOffset = new PChunkPos((int)worldOffset.x, (int)worldOffset.y, (int)worldOffset.z);
         this.rtToWorldBlockOffset = new PChunkPos(this.rtToWorldChunkOffset.x, this.rtToWorldChunkOffset.y, this.rtToWorldChunkOffset.z).toBlockPos();
         this.synchronizeChunks();
         this.dirty = this.update(this.rootMemoryManager);
         if (this.dirty && this.blockLightEnabled) {
            this.lightRegistry.compileRegistry(this.rtToWorldBlockOffset, previousBlockOffset);
         }

         this.changeBuildStage(WorldRegistry.BuildStage.WAIT_FOR_UPLOAD);
      }
   }

   public void synchronizeChunks() {
      this.ensureWorldThread();
      Set<PChunkPos> inboundNonEmptyChunks = new HashSet<>();

      for (PChunkPos chunkPos : this.renderDispatcher.getInboundChunks()) {
         if (!this.renderDispatcher.isChunkEmpty(chunkPos)) {
            PChunkPos rtChunkPos = new PChunkPos(
               chunkPos.x - this.rtToWorldChunkOffset.x, chunkPos.y - this.rtToWorldChunkOffset.y, chunkPos.z - this.rtToWorldChunkOffset.z
            );
            if (rtChunkPos.x >= 0
               && rtChunkPos.x < this.worldChunkSize
               && rtChunkPos.y >= 0
               && rtChunkPos.y < this.worldChunkSize
               && rtChunkPos.z >= 0
               && rtChunkPos.z < this.worldChunkSize) {
               inboundNonEmptyChunks.add(chunkPos);
            }
         }
      }

      for (PChunkPos chunkPos : inboundNonEmptyChunks) {
         if (!this.chunks.containsKey(chunkPos)) {
            this.loadChunk(chunkPos);
         }
      }

      for (PChunkPos chunkPos : this.chunks.keySet().toArray(new PChunkPos[0])) {
         if (!inboundNonEmptyChunks.contains(chunkPos)) {
            this.unloadChunk(chunkPos);
         }
      }

      this.worldMinVoxel = new PBlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
      this.worldMaxVoxel = new PBlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

      for (PChunkPos chunkPos : this.chunks.keySet()) {
         PBlockPos blockPos = chunkPos.toBlockPos();
         this.worldMinVoxel.min(blockPos.x, blockPos.y, blockPos.z);
         this.worldMaxVoxel.max(blockPos.x, blockPos.y, blockPos.z);
      }

      this.worldMinVoxel.sub(this.rtToWorldBlockOffset.x, this.rtToWorldBlockOffset.y, this.rtToWorldBlockOffset.z);
      this.worldMinVoxel.scale(16, 16, 16);
      this.worldMaxVoxel.add(16, 16, 16);
      this.worldMaxVoxel.sub(this.rtToWorldBlockOffset.x, this.rtToWorldBlockOffset.y, this.rtToWorldBlockOffset.z);
      this.worldMaxVoxel.scale(16, 16, 16);
   }

   public void loadChunk(PChunkPos chunkPos) {
      this.ensureWorldThread();
      this.onChunkLoad(chunkPos);
      PChunk chunk = this.chunks.computeIfAbsent(chunkPos, k -> {
         PChunk newChunk = new PChunk();
         newChunk.allocate(this.cbMemoryManager);
         return newChunk;
      });
      chunk.freeBlocks();

      for (int x = 0; x < 16; x++) {
         for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
               PBlockPos pBlockPos = new PBlockPos(16 * chunkPos.x + x, 16 * chunkPos.y + y, 16 * chunkPos.z + z);
               BlockPos blockPos = new BlockPos(pBlockPos.x, pBlockPos.y, pBlockPos.z);
               PBlock block = this.blockRegistry.getBlock(pBlockPos);
               if (block == null) {
                  chunk.set(x, y, z, null, -1);
               } else {
                  int skyBrightness = 0;
                  Level level = Minecraft.getInstance().level;
                  if (level != null) {
                     LevelLightEngine lightEngine = level.getLightEngine();
                     LayerLightEventListener layerListener = lightEngine.getLayerListener(LightLayer.SKY);
                     int brightness = layerListener.getLightValue(blockPos) / 2;

                     for (int i = 5; i >= 0; i--) {
                        skyBrightness = skyBrightness << 3 | Math.max(layerListener.getLightValue(blockPos.relative(FACES[i])) / 2, brightness);
                     }
                  }

                  if (!block.isUsed() || !block.isAllocated()) {
                     synchronized (block) {
                        this.blockRegistry.ensureAllocated(block);
                        if (block.getMemory() == null) {
                           block = null;
                           skyBrightness = -1;
                        }
                     }
                  }

                  chunk.set(x, y, z, block, skyBrightness);
               }
            }
         }
      }
   }

   public void unloadChunk(PChunkPos chunkPos) {
      this.ensureWorldThread();
      PChunk chunk = this.chunks.remove(chunkPos);
      if (chunk != null) {
         chunk.free(this.cbMemoryManager);
         chunk.freeBlocks();
      }
   }

   @Override
   public void allocate(MemoryManager memoryManager) {
      this.rootMemory = memoryManager.allocate(this.getSize());
   }

   @Override
   public void free(MemoryManager memoryManager) {
      memoryManager.free(this.rootMemory);
      this.rootMemory = null;
   }

   @Override
   public boolean update(MemoryManager memoryManager) {
      Arrays.fill(this.rootSchematic.getData(), 0);

      for (Entry<PChunkPos, PChunk> entry : this.chunks.entrySet()) {
         PChunkPos chunkPos = entry.getKey();
         PChunk chunk = entry.getValue();
         PChunkPos rtChunkPos = new PChunkPos(chunkPos.x, chunkPos.y, chunkPos.z);
         rtChunkPos.sub(this.rtToWorldChunkOffset.x, this.rtToWorldChunkOffset.y, this.rtToWorldChunkOffset.z);
         if (rtChunkPos.x >= 0
            && rtChunkPos.x < this.worldChunkSize
            && rtChunkPos.y >= 0
            && rtChunkPos.y < this.worldChunkSize
            && rtChunkPos.z >= 0
            && rtChunkPos.z < this.worldChunkSize) {
            chunk.update(this.cbMemoryManager);
            this.rootSchematic.setEntry(rtChunkPos.x, rtChunkPos.y, rtChunkPos.z, chunk.getMemory().begin >> 2);
         }
      }

      try {
         this.rootSchematic.reset();
         this.rootSchematic.initialize();
         this.rootSchematic.optimizeThreaded().get();
         this.rootMemory.getBuffer().asIntBuffer().put(this.rootSchematic.getData());
         memoryManager.queueUpload(this);
         return true;
      } catch (InterruptedException | ExecutionException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public void afterUpload() {
      this.dirty = false;
   }

   @Override
   public int getSize() {
      return 4 * this.worldChunkSize * this.worldChunkSize * this.worldChunkSize;
   }

   @Override
   public MemoryRegion getMemory() {
      return this.rootMemory;
   }

   private void onChunkLoad(PChunkPos chunkPos) {
      Vector3d cameraPosition = MinecraftAccessor.getCameraPosition();
      PBlockPos chunkBlockPos = chunkPos.toBlockPos();
      double cameraDistance = new Vector3d(chunkBlockPos.x, chunkBlockPos.y, chunkBlockPos.z)
         .add(new Vector3f(8.0F))
         .add(new Vector3f((float)cameraPosition.x, (float)cameraPosition.y, (float)cameraPosition.z).mul(-1.0F))
         .length();
      if (cameraDistance < 32.0) {
         this.closeChunkUpdate = true;
      }
   }

   @Override
   public void free() {
      this.worldCompilerThread.free();
      this.buildQueue.clear();
      this.glQueue.clear();
      this.lightRegistry.free();
      this.blockRegistry.free();
      this.cbMemoryManager.free();
      this.rootMemoryManager.free();
   }

   public Map<PChunkPos, PChunk> getChunks() {
      return this.chunks;
   }

   public void ensureWorldThread() {
      if (Thread.currentThread() != this.worldCompilerThread) {
         throw new IllegalStateException("Called from wrong thread!");
      }
   }

   public void queueBuildJob(Runnable job) {
      this.buildQueue.add(job);
   }

   public void queueGlJob(Runnable job) {
      this.glQueue.add(job);
   }

   public Vector3d toRt(Vector3d vector3f) {
      return new Vector3d(vector3f).sub(this.liveWorldBlockOffset);
   }

   public boolean fetchLightReload() {
      boolean lightReload = this.closeChunkUpload;
      this.closeChunkUpload = false;
      return lightReload;
   }

   public void startWorldBuilder() {
      this.worldCompilerThread.ensureRunning();
   }

   public void stopWorldBuilder() {
      this.worldCompilerThread.sendStopSignal();
   }

   public void wakeUpWorldBuilder() {
      synchronized (this.worldCompilerThread) {
         this.worldCompilerThread.notifyAll();
      }
   }

   public Vector3d getWorldOffset() {
      return this.liveWorldBlockOffset;
   }

   public ConcurrentLinkedQueue<Runnable> getGlQueue() {
      return this.glQueue;
   }

   public void changeBuildStage(WorldRegistry.BuildStage buildStage) {
      if (buildStage.before != this.buildStage) {
         throw new IllegalStateException();
      }

      this.buildStage = buildStage;
   }

   public int getFirstBuildTime() {
      return this.firstBuildTime;
   }

   public int getLastBuildTime() {
      return this.lastBuildTime;
   }

   public GlMemoryManager getRootMemoryManager() {
      return this.rootMemoryManager;
   }

   public GlMemoryManager getCbMemoryManager() {
      return this.cbMemoryManager;
   }

   public LightRegistry getLightRegistry() {
      return this.lightRegistry;
   }

   public BlockRegistry getBlockRegistry() {
      return this.blockRegistry;
   }

   public PBlockPos getWorldMinVoxel() {
      return this.liveWorldMinVoxel;
   }

   public PBlockPos getWorldMaxVoxel() {
      return this.liveWorldMaxVoxel;
   }

   private static int getFaceIndexFromNormal(Direction direction) {
      Vec3i normal = direction.getNormal();
      float x = normal.getX();
      float y = normal.getY();
      float z = normal.getZ();
      return Math.clamp((int)(Math.abs(x) * (x * 0.5 + 0.5) + Math.abs(y) * (y * 0.5 + 2.5) + Math.abs(z) * (z * 0.5 + 4.5) + 0.5), 0, 5);
   }

   static {
      WorldRegistry.BuildStage.IDLE.before = WorldRegistry.BuildStage.UPLOAD;
      WorldRegistry.BuildStage.COMPILE.before = WorldRegistry.BuildStage.IDLE;
      WorldRegistry.BuildStage.WAIT_FOR_UPLOAD.before = WorldRegistry.BuildStage.COMPILE;
      WorldRegistry.BuildStage.UPLOAD.before = WorldRegistry.BuildStage.WAIT_FOR_UPLOAD;

      for (Direction dir : Direction.values()) {
         FACES[getFaceIndexFromNormal(dir)] = dir;
      }
   }

   public enum BuildStage {
      IDLE,
      COMPILE,
      WAIT_FOR_UPLOAD,
      UPLOAD;

      public WorldRegistry.BuildStage before;
   }
}
