package at.redi2go.photonics.client.rendering.world;

import at.redi2go.photonics.client.Photonics;
import at.redi2go.photonics.client.Raytracer;
import at.redi2go.photonics.client.config.PhotonicsConfig;
import at.redi2go.photonics.client.config.ShaderPackLights;
import at.redi2go.photonics.client.config.lights.BlockLightInfo;
import at.redi2go.photonics.client.config.lights.LightList;
import at.redi2go.photonics.client.config.lights.LightsProvider;
import at.redi2go.photonics.client.mixin.ShaderPackAccessor;
import at.redi2go.photonics.client.rendering.MinecraftAccessor;
import at.redi2go.photonics.client.rendering.opengl.objects.Destructable;
import at.redi2go.photonics.client.rendering.opengl.objects.GlTarget;
import at.redi2go.photonics.client.rendering.util.IrisUtil;
import at.redi2go.photonics.client.rendering.util.MultiThreader;
import at.redi2go.photonics.client.rendering.world.buffer.GlMemoryManager;
import at.redi2go.photonics.client.rendering.world.buffer.MemoryOwner;
import at.redi2go.photonics.client.rendering.world.buffer.SimpleMemoryOwner;
import at.redi2go.photonics.client.rendering.world.position.LightNodePos;
import at.redi2go.photonics.client.rendering.world.position.PBlockPos;
import at.redi2go.photonics.client.rendering.world.position.PChunkPos;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

interface IntIntBiConsumer {
    void accept(int a, int b);
}

public class LightRegistry implements Destructable {
   private static final int LIGHT_DATA_SIZE = 12;
   private static final int LIGHT_BYTE_SIZE = 48;
   private final Predicate<PChunkPos> chunkEmptyPredicate;
   private final boolean lightBinningEnabled;
   @Nullable
   private final GlMemoryManager registryMemoryManager;
   @Nullable
   private final MemoryOwner registryMemory;
   private final GlMemoryManager lightsMemoryManager;
   private final MemoryOwner lightsMemory;
   private final GlMemoryManager lightMappingMemoryManager;
   private final MemoryOwner lightMappingMemory;
   private final int maxLights;
   private final int maxLightsPerNode;
   private final int nodeSize;
   private final int worldSize;
   private final int nodeCount;
   private short[] lightRegions;
   private short[] lightRegionsPrev;
   private short[] newLightIndices;
   private PBlockPos offset = new PBlockPos(0, 0, 0);
   private int lightCount = 0;
   private int lastLoggedLightCount = -1;
   private LightInstance[] tracedLights = new LightInstance[0];
   private final Map<Vector3f, TracedLightPosition> tracedLightPositions = new ConcurrentHashMap<>();
   private final PhotonicsConfig.Observer<LightList> lightListObserver;
   private LightList lightList = new LightList();
   private final ReadWriteLock lock;
   private boolean building = false;
   private int compileCount = 0;
   @Nullable
   private LightsProvider lightsProvider;
   private LightInstance[] prevLights = null;
   private int prevChanges = -1;
   private static final Vec3i[] NEIGHBORS = new Vec3i[]{
      new Vec3i(0, 1, 0), new Vec3i(0, -1, 0), new Vec3i(1, 0, 0), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(0, 0, -1)
   };

   public LightRegistry(int maxLights, int maxLightsPerNode, int nodeSize, int worldSize, Predicate<PChunkPos> chunkEmptyPredicate, boolean lightBinningEnabled) {
      if (16 % nodeSize != 0) {
         throw new IllegalArgumentException();
      }

      this.chunkEmptyPredicate = chunkEmptyPredicate;
      this.maxLights = maxLights;
      this.maxLightsPerNode = maxLightsPerNode;
      this.nodeSize = nodeSize;
      this.worldSize = worldSize;
      this.nodeCount = worldSize / nodeSize;
      int lightSize = this.nodeCount * this.nodeCount * this.nodeCount * (1 + maxLightsPerNode);
      this.lightBinningEnabled = lightBinningEnabled;
      if (lightBinningEnabled) {
         this.registryMemoryManager = new GlMemoryManager(GlTarget.SSBO, "light_registry_block", 4 * lightSize, false);
         this.registryMemory = new SimpleMemoryOwner(this.registryMemoryManager, this.registryMemoryManager.getCapacity());
         this.lightRegions = new short[lightSize];
         this.lightRegionsPrev = new short[lightSize];
      } else {
         this.registryMemoryManager = null;
         this.registryMemory = null;
         this.lightRegions = null;
         this.lightRegionsPrev = null;
      }

      this.newLightIndices = new short[maxLights];
      this.lightsMemoryManager = new GlMemoryManager(GlTarget.SSBO, "ph_light_list", maxLights * 48 + 4, true);
      this.lightsMemory = new SimpleMemoryOwner(this.lightsMemoryManager, this.lightsMemoryManager.getCapacity());
      this.lightMappingMemoryManager = new GlMemoryManager(GlTarget.SSBO, "ph_light_list_mapping", maxLights * 4, false);
      this.lightMappingMemory = new SimpleMemoryOwner(this.lightMappingMemoryManager, this.lightMappingMemoryManager.getCapacity());
      this.lightListObserver = PhotonicsConfig.observe(c -> PhotonicsConfig.getLightList(), c -> {
         this.lightList = c;
         this.registerLightBlocks(this.lightList);
         Minecraft.getInstance().levelRenderer.allChanged();
      });
      this.lock = new ReentrantReadWriteLock();
   }

   public Lock readLock() {
      return this.lock.readLock();
   }

   public void init() {
      this.lightList = PhotonicsConfig.getLightList();
      this.registerLightBlocks(this.lightList);
      String shaderPackName = (String)Iris.getIrisConfig().getShaderPackName().orElseThrow(() -> new RuntimeException("No shaderpack selected!"));
      this.lightsProvider = Iris.getCurrentPack().map(it -> (ShaderPackAccessor)it).flatMap(it -> {
         String contents = it.getSourceProvider().apply(AbsolutePackPath.fromAbsolutePath("/ph_lights.json"));
         if (contents == null) {
            return Optional.empty();
         }

         try {
            ShaderPackLights lights = ShaderPackLights.parse(contents);
            lights.setShaderPack((ShaderPack)it);
            PhotonicsConfig.registerLightProvider(lights);
            return Optional.of(lights);
         } catch (Exception e) {
            Photonics.error("Error while parsing ph_lights.json for " + shaderPackName, e);
            return Optional.empty();
         }
      }).orElse(null);
   }

   public int totalLights() {
      return this.tracedLightPositions.size();
   }

   public int lightCount() {
      return this.lightCount;
   }

   public void compileRegistry(PBlockPos blockOffset, PBlockPos previousOffset) {
      if (this.building) {
         throw new IllegalStateException();
      }

      ClientLevel level = MinecraftAccessor.getLevel();
      if (level != null) {
         this.building = true;
         this.offset = blockOffset;
         this.lock.writeLock().lock();

         LightInstance[] lights;
         try {
            if (++this.compileCount % 10 == 0) {
               this.compileCount = 0;
               lights = this.toLightInstanceArrayClearUnloaded(level);
            } else {
               lights = this.toLightInstanceArray();
            }
         } finally {
            this.lock.writeLock().unlock();
         }

         this.createTracedLights(lights, previousOffset);
         if (this.lightBinningEnabled) {
            short[] swap = this.lightRegionsPrev;
            this.lightRegionsPrev = this.lightRegions;
            this.lightRegions = swap;
            MultiThreader.runAndWait(this.nodeCount, x -> {
               for (int y = 0; y < this.nodeCount; y++) {
                  for (int z = 0; z < this.nodeCount; z++) {
                     LightNodePos lightNodePos = new LightNodePos(x, y, z);
                     if (!this.chunkEmptyPredicate.test(lightNodePos.toBlockPos(this.nodeSize, blockOffset).toChunkPos())) {
                        this.compileAndStoreNode(lightNodePos, previousOffset);
                     }
                  }
               }
            });
            this.registryMemoryManager.queueUpload(this.registryMemory);
         }

         this.storeLights();
         this.lightsMemoryManager.queueUpload(this.lightsMemory);
         this.building = false;
      }
   }

   private int toLightIndex(int x, int y, int z) {
      return (1 + this.maxLightsPerNode) * (y * this.nodeCount * this.nodeCount + z * this.nodeCount + x);
   }

   private int toLightIndex(LightNodePos pos) {
      return (1 + this.maxLightsPerNode) * (pos.y * this.nodeCount * this.nodeCount + pos.z * this.nodeCount + pos.x);
   }

   public void markDirtyRegions(PBlockPos pos, float lightRadius, PBlockPos previousOffset) {
      int radius = (int)Math.ceil(lightRadius / this.nodeSize);
      LightNodePos start = pos.toLightPos(this.nodeSize, previousOffset);
      LightNodePos end = new LightNodePos(start);
      start.sub(radius);
      end.add(radius);

      for (int x = start.x; x <= end.x; x++) {
         for (int y = start.y; y <= end.y; y++) {
            for (int z = start.z; z <= end.z; z++) {
               int index = this.toLightIndex(x, y, z);
               if (index >= 0 && index < this.lightRegions.length) {
                  this.lightRegions[index] = (short)(this.lightRegions[index] & 32767);
               }
            }
         }
      }
   }

   private boolean compileAndStoreNode(LightNodePos pos, PBlockPos previousOffset) {
      PBlockPos blockPos = pos.toBlockPos(this.nodeSize, this.offset);
      IntBuffer buffer = this.registryMemory.getMemory().getBuffer().asIntBuffer();
      int curIndex = this.toLightIndex(pos);
      LightNodePos prevLightPos = blockPos.toLightPos(this.nodeSize, previousOffset);
      int prevIndex = this.toLightIndex(prevLightPos);
      if (prevIndex >= 0 && prevIndex < this.lightRegions.length) {
         short count = this.lightRegionsPrev[prevIndex];
         if ((count & -32768) != 0 && prevIndex + (count & 32767) < this.lightRegionsPrev.length) {
            int length = count & 32767;
            buffer.put(curIndex, length);
            this.lightRegions[curIndex] = count;

            for (int i = 1; i <= length; i++) {
               short light = this.lightRegionsPrev[prevIndex + i];
               light = this.newLightIndices[light];
               buffer.put(curIndex + i, light);
               this.lightRegions[curIndex + i] = light;
            }

            return true;
         }
      }

      Vector3f middleBlockPos = new Vector3f(blockPos.x + this.nodeSize * 0.5F, blockPos.y + this.nodeSize * 0.5F, blockPos.z + this.nodeSize * 0.5F);
      float[] luminanceCache = new float[this.tracedLights.length];
      AbstractQueue<LightInstance> sortedLights = new PriorityQueue<>(this.maxLightsPerNode, compareWithCache(luminanceCache));

      for (LightInstance light : this.tracedLights) {
         float luminance = light.type().luminanceFrom(light.position(), middleBlockPos);
         luminanceCache[light.index()] = luminance;
         if (!(luminance <= 0.001F)) {
            sortedLights.add(light);
            if (sortedLights.size() > this.maxLightsPerNode) {
               sortedLights.poll();
            }
         }
      }

      buffer.put(curIndex, sortedLights.size());
      this.lightRegions[curIndex] = (short)(sortedLights.size() | -32768);

      for (int offset = sortedLights.size(); !sortedLights.isEmpty(); offset--) {
         int light = sortedLights.remove().index();
         buffer.put(curIndex + offset, light);
         this.lightRegions[curIndex + offset] = (short)light;
      }

      return false;
   }

   private void storeLights() {
      FloatBuffer buffer = this.lightsMemory.getMemory().getBuffer().asFloatBuffer();

      for (LightInstance light : this.tracedLights) {
         buffer.position(12 * light.index());
         BlockLightInfo lightInfo = light.type();
         Vector4f[] data = lightInfo.toVector4Array(light.position(), light.blockId());

         for (Vector4f vec : data) {
            store(vec, buffer);
         }
      }
   }

   private void createTracedLights(LightInstance[] lights, PBlockPos previousOffset) {
      if (lights.length > this.maxLights) {
         Vector3d pos = MinecraftAccessor.getCameraPosition();
         Vector3f cameraPosition = new Vector3f((float)pos.x, (float)pos.y, (float)pos.z);
         Arrays.sort(lights, Comparator.comparingDouble(l -> -l.type().luminanceFrom(l.position(), cameraPosition)));
         lights = Arrays.copyOf(lights, this.maxLights);
      }

      if (this.lightBinningEnabled) {
         this.createLightMapping(this.tracedLights, lights, (before, after) -> {
            if (after != -1) {
               this.newLightIndices[before] = (short)after;
            }
         }, (pos, before, after) -> {
            float radius = 0.0F;
            if (before != null) {
               radius = before.radiusInBlocks();
            }

            if (after != null) {
               radius = Math.max(radius, after.radiusInBlocks());
            }

            this.markDirtyRegions(new PBlockPos(pos.x, pos.y, pos.z), radius, previousOffset);
         });
      } else {
         for (int i = 0; i < lights.length; i++) {
            lights[i].setIndex(i);
         }
      }

      this.tracedLights = lights;
   }

   public void clearLightMappings() {
      if (this.prevChanges != 0) {
         this.prevChanges = 0;
         IntBuffer buffer = this.lightMappingMemory.getMemory().getBuffer().asIntBuffer();

         for (int i = 0; i < this.maxLights; i++) {
            buffer.put(i, i);
         }

         this.lightMappingMemoryManager.queueUpload(this.lightMappingMemory);
         this.lightMappingMemoryManager.upload();
      }
   }

   public boolean upload() {
      if (this.prevLights != null && this.prevLights != this.tracedLights) {
         IntBuffer buffer = this.lightMappingMemory.getMemory().getBuffer().asIntBuffer();
         int[] changes = new int[]{0};
         this.createLightMapping(this.prevLights, this.tracedLights, (before, after) -> {
            if (before != after) {
               changes[0]++;
            }

            buffer.put(before, after);
         }, (p0, p1, p2) -> {});
         if (this.prevChanges != 0 || changes[0] != 0) {
            this.lightMappingMemoryManager.queueUpload(this.lightMappingMemory);
         }

         this.prevChanges = changes[0];
      }

      this.prevLights = this.tracedLights;
      boolean uploadDone = true;
      if (this.lightBinningEnabled) {
         uploadDone &= this.registryMemoryManager.upload();
      }

      uploadDone &= this.lightsMemoryManager.upload();
      uploadDone &= this.lightMappingMemoryManager.upload();
      this.lightCount = this.tracedLights.length;
      if (this.lightCount != this.lastLoggedLightCount) {
         this.lastLoggedLightCount = this.lightCount;
         Photonics.info("Traced light diagnostics: active={}, discovered={}", this.lightCount, this.tracedLightPositions.size());
      }
      return uploadDone;
   }

   public void registerBlockState(BlockState blockState, PBlock pBlock) {
      BlockLightInfo lightInfo = this.lightList.get(blockState);
      if (lightInfo != null) {
         if (lightInfo.isTraced()) {
            pBlock.setEmissionColor(new Vector3f(0.0F));
         } else {
            pBlock.setEmissionColor(lightInfo.getColorAsVector());
         }
      }
   }

   private void registerLightBlocks(LightList lights) {
      Raytracer.INSTANCE
         .getBlockRegistry()
         .getBlockSchematicCache()
         .entrySet()
         .stream()
         .map(e -> Pair.of(e.getValue(), lights.get(e.getKey())))
         .filter(e -> e.getValue() != null)
         .forEach(entry -> {
            PBlock pBlock = (PBlock)entry.getKey();
            BlockLightInfo lightInfo = (BlockLightInfo)entry.getValue();
            if (lightInfo.isTraced()) {
               pBlock.setEmissionColor(new Vector3f(0.0F));
            } else {
               pBlock.setEmissionColor(lightInfo.getColorAsVector());
            }
         });
   }

   public void onBlockLoad(Vector3f position) {
      ClientLevel level = MinecraftAccessor.getLevel();
      if (level != null) {
         BlockPos blockPos = new BlockPos((int)position.x, (int)position.y, (int)position.z);
         BlockState blockState = level.getBlockState(blockPos);
         int blockId = IrisUtil.getBlockId(blockState);
         BlockLightInfo lightInfo = this.lightList.get(blockState);
         Vector3f lightPos = new Vector3f(position).add(0.5F, 0.5F, 0.5F);
         if (lightInfo != null && lightInfo.isTraced() && !shouldCull(level, blockPos)) {
            this.tracedLightPositions.put(lightPos, new TracedLightPosition(blockId, lightInfo));
         } else {
            this.tracedLightPositions.remove(lightPos);
         }
      }
   }

   public boolean isLightBinningEnabled() {
      return this.lightBinningEnabled;
   }

   @Nullable
   public GlMemoryManager getRegistryMemoryManager() {
      return this.registryMemoryManager;
   }

   public GlMemoryManager getLightsMemoryManager() {
      return this.lightsMemoryManager;
   }

   public GlMemoryManager getLightMappingMemoryManager() {
      return this.lightMappingMemoryManager;
   }

   private LightInstance[] toLightInstanceArray() {
      LightInstance[] lights = new LightInstance[this.tracedLightPositions.size()];
      int i = 0;

      for (Entry<Vector3f, TracedLightPosition> e : this.tracedLightPositions.entrySet()) {
         lights[i++] = new LightInstance(e.getValue().blockId(), e.getKey(), e.getValue().lightInfo());
      }

      return lights;
   }

   private LightInstance[] toLightInstanceArrayClearUnloaded(ClientLevel level) {
      List<LightInstance> lights = new ArrayList<>(this.tracedLightPositions.size());
      Iterator<Entry<Vector3f, TracedLightPosition>> itr = this.tracedLightPositions.entrySet().iterator();

      while (itr.hasNext()) {
         Entry<Vector3f, TracedLightPosition> e = itr.next();
         Vector3f pos = e.getKey();
         TracedLightPosition tracedPos = e.getValue();
         BlockLightInfo light = tracedPos.lightInfo();
         BlockPos blockPos = new BlockPos((int)pos.x, (int)pos.y, (int)pos.z);
         if (!level.isLoaded(blockPos)) {
            itr.remove();
         } else {
            lights.add(new LightInstance(tracedPos.blockId(), pos, light));
         }
      }

      return lights.toArray(LightInstance[]::new);
   }

   @Override
   public void free() {
      this.lightsMemoryManager.free();
      this.lightMappingMemoryManager.free();
      if (this.registryMemoryManager != null) {
         this.registryMemoryManager.free();
      }

      this.lightListObserver.unregister();
      if (this.lightsProvider != null) {
         PhotonicsConfig.removeLightProvider(this.lightsProvider);
      }
   }

   private static void store(Vector4f vector4f, FloatBuffer buffer) {
      buffer.put(vector4f.x);
      buffer.put(vector4f.y);
      buffer.put(vector4f.z);
      buffer.put(vector4f.w);
   }

   private static void store(Vector3f vector3f, FloatBuffer buffer) {
      buffer.put(vector3f.x);
      buffer.put(vector3f.y);
      buffer.put(vector3f.z);
   }

   private static void store(Vector2f vector2f, FloatBuffer buffer) {
      buffer.put(vector2f.x);
      buffer.put(vector2f.y);
   }

   private void createLightMapping(
      LightInstance[] previous, LightInstance[] current, IntIntBiConsumer mapping, TriConsumer<Vector3i, BlockLightInfo, BlockLightInfo> diffConsumer
   ) {
      Object2ObjectOpenHashMap<Vector3i, LightInvalidation> differences = new Object2ObjectOpenHashMap(Math.max(previous.length, current.length));
      int i = 0;

      while (i < previous.length) {
         LightInstance light = previous[i];
         Vector3i pos = light.position().get(2, new Vector3i());
         LightInvalidation difference = (LightInvalidation)differences.computeIfAbsent(pos, LightInvalidation::new);
         difference.before = light.type();
         difference.beforeIndex = i++;
      }

      for (int ix = 0; ix < current.length; ix++) {
         LightInstance light = current[ix];
         Vector3i pos = light.position().get(2, new Vector3i());
         LightInvalidation difference = (LightInvalidation)differences.computeIfAbsent(pos, LightInvalidation::new);
         difference.after = light.type();
         difference.afterIndex = ix;
         light.setIndex(ix);
      }

      ObjectIterator var15 = differences.entrySet().iterator();

      while (var15.hasNext()) {
         Entry<Vector3i, LightInvalidation> e = (Entry<Vector3i, LightInvalidation>)var15.next();
         Vector3i pos = e.getKey();
         LightInvalidation difference = e.getValue();
         BlockLightInfo before = difference.before;
         int beforeIndex = difference.beforeIndex;
         BlockLightInfo after = difference.after;
         int afterIndex = difference.afterIndex;
         if (before == after) {
            mapping.accept(beforeIndex, afterIndex);
         } else {
            if (beforeIndex != -1) {
               mapping.accept(beforeIndex, -1);
            }

            diffConsumer.accept(pos, before, after);
         }
      }
   }

   private static Comparator<LightInstance> compareWithCache(float[] luminanceCache) {
      return (e1, e2) -> Float.compare(luminanceCache[e1.index()], luminanceCache[e2.index()]);
   }

   private static boolean shouldCull(Level level, BlockPos blockPos) {
      for (Vec3i offset : NEIGHBORS) {
         BlockState neighbor = level.getBlockState(new BlockPos(blockPos.offset(offset)));
         Block neighborBlock = neighbor.getBlock();
         if (neighborBlock != Blocks.LAVA && (!neighbor.isSuffocating(level, blockPos) || !neighbor.isCollisionShapeFullBlock(level, blockPos))) {
            return false;
         }
      }

      return true;
   }
}
