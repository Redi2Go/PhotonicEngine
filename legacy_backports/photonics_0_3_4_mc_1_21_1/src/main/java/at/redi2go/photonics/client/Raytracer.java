package at.redi2go.photonics.client;

import at.redi2go.photonics.client.api.LightingMode;
import at.redi2go.photonics.client.api.PhotonicsProperties;
import at.redi2go.photonics.client.config.PhotonicsConfig;
import at.redi2go.photonics.client.mixin.ShaderPackAccessor;
import at.redi2go.photonics.client.rendering.opengl.objects.Destructable;
import at.redi2go.photonics.client.rendering.opengl.rendering.ColorFramebuffer;
import at.redi2go.photonics.client.rendering.opengl.rendering.ShaderUtil;
import at.redi2go.photonics.client.rendering.opengl.rendering.renderers.MainRenderer;
import at.redi2go.photonics.client.rendering.patching.Patch;
import at.redi2go.photonics.client.rendering.TemporalUniformState;
import at.redi2go.photonics.client.rendering.world.BlockRegistry;
import at.redi2go.photonics.client.rendering.world.VoxelFallbackDiagnostics;
import at.redi2go.photonics.client.rendering.world.WorldRegistry;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.include.IncludeGraph;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class Raytracer implements Destructable {
   public static final Object LOCK = new Object();
   public static Raytracer INSTANCE;
   public static final Path SHADER_PATCHES_PATH = Path.of("./shader-patches");
   public static final Path DEV_ENV_SHADERS_PATH = Path.of("../src/main/resources/assets/photonics/shaders");
   private static final Set<String> AUTO_REPLACED_FILES = Set.of("photonics.glsl", "ph_samplers.glsl");
   public static Map<String, String> SHADERPACK_CHANGED_OPTIONS = null;
   public static Properties SHADERPACK_PROPERTIES = null;
   public static Properties PATCHED_SHADERPACK_PROPERTIES = null;
   public static final IntSet USED_BUFFERS = new IntOpenHashSet();
   public static ColorFramebuffer CURRENT_FRAMEBUFFER = null;
   private static final List<FileSystem> FILE_SYSTEMS = new ArrayList<>();
   public static final List<Patch> AVAILABLE_PATCHES = new ArrayList<>();
   public static final TerrainRenderPass VOXEL = new TerrainRenderPass(RenderType.cutout(), false, true);
   private final MainRenderer mainRenderer;
   private final RenderDispatcher renderDispatcher;
   private final WorldRegistry worldRegistry;
   public final Path shaderPackPath;
   private final PhotonicsConfig.Observer<Set<Block>> voxelizedBlockObserver;

   public Raytracer() {
      String shaderPackName = (String)Iris.getIrisConfig().getShaderPackName().orElseThrow(() -> new RuntimeException("No shaderpack selected!"));
      this.shaderPackPath = Iris.getShaderpacksDirectory().resolve(shaderPackName);
      ShaderPack buffers = (ShaderPack)Iris.getCurrentPack().orElseThrow();
      USED_BUFFERS.clear();
      IntIterator properties = buffers.getBufferObjects().keySet().iterator();

      while (properties.hasNext()) {
         Integer index = (Integer)properties.next();
         USED_BUFFERS.add(index);
      }

      PhotonicsProperties propertiesx = getProperties().orElseThrow();
      VoxelFallbackDiagnostics.reset();
      TemporalUniformState.INSTANCE.reset();
      this.renderDispatcher = new RenderDispatcher(propertiesx.getRenderScale(), propertiesx);
      this.worldRegistry = new WorldRegistry(
         this.renderDispatcher,
         propertiesx.getMaxLights(),
         propertiesx.getMaxSamples(),
         propertiesx.isBlockLightEnabled().orElse(true),
         propertiesx.isLightBinningEnabled().orElse(propertiesx.getLightingMode() != LightingMode.OFF)
      );
      this.mainRenderer = propertiesx.getLightingMode().createMainRenderer(this.worldRegistry, propertiesx.getRenderScale(), propertiesx);
      this.worldRegistry.startWorldBuilder();
      this.voxelizedBlockObserver = PhotonicsConfig.observe(c -> c.voxelizedBlocks, unused -> Minecraft.getInstance().levelRenderer.allChanged());
      Minecraft minecraft = Minecraft.getInstance();
      int framebufferWidth = Math.max(1, Math.round(minecraft.getWindow().getWidth() * propertiesx.getRenderScale()));
      int framebufferHeight = Math.max(1, Math.round(minecraft.getWindow().getHeight() * propertiesx.getRenderScale()));
      String world = minecraft.level == null ? "none" : minecraft.level.dimension().location().toString();
      Photonics.info(
         "Pipeline diagnostics: shaderPack={}, lightingMode={}, giEnabled={}, renderScale={}, framebuffer={}x{}, maxLights={}, maxSamples={}, "
            + "restirInitialSamples={}, restirSpatialReuseSamples={}, restirSpatialReuseRadius={}, restirAccumulationFrames={}, "
            + "restirDenoiserPassesRequested={}, configuredVoxelBlocks={}, world={}",
         shaderPackName,
         propertiesx.getLightingMode(),
         propertiesx.isGiEnabled().orElse(true),
         propertiesx.getRenderScale(),
         framebufferWidth,
         framebufferHeight,
         propertiesx.getMaxLights(),
         propertiesx.getMaxSamples(),
         propertiesx.getRestirInitialSamples(),
         propertiesx.getRestirSpatialReuseSamples(),
         propertiesx.getRestirSpatialReuseRadius(),
         propertiesx.getRestirAccumulationFrames(),
         propertiesx.getRestirDenoiserPasses(),
         PhotonicsConfig.getVoxelizedBlocks().size(),
         world
      );
   }

   public void queueBuildJob(Runnable job) {
      this.worldRegistry.queueBuildJob(job);
   }

   public void queueUrgentBuildJob(Runnable job) {
      this.queueBuildJob(job);
      this.worldRegistry.wakeUpWorldBuilder();
   }

   public void queueOpenGLJob(Runnable job) {
      this.worldRegistry.queueGlJob(job);
   }

   public static void filterDoubleFaces(Map<Direction, BlockElementFace> faces, Vector3f from, Vector3f to) {
      Vector3f delta = new Vector3f(from).add(-to.x, -to.y, -to.z);
      if (delta.x * delta.y * delta.z == 0.0F) {
         Vector3f normal = new Vector3f(to).add(from).mul(0.03125F).add(new Vector3f(-0.5F, -0.5F, -0.5F));
         normal.mul(1.0F / normal.length());
         Direction normalDirection = Arrays.stream(Direction.values())
            .filter(direction -> Vec3.atLowerCornerOf(direction.getNormal()).dot(new Vec3(normal.x, normal.y, normal.z)) > Math.cos(Math.toRadians(45.0)))
            .findFirst()
            .orElse(null);
         if (normalDirection != null) {
            faces.forEach((key, value) -> {
               Direction oppositeDirection = key.getOpposite();
               if (faces.containsKey(oppositeDirection)) {
                  if (key == normalDirection) {
                     ((BlockElementFaceExt)(Object)value).photonics$setShouldBeVoxelized(false);
                  }
               }
            });
         }
      }
   }

   @Override
   public void free() {
      synchronized (LOCK) {
         this.renderDispatcher.free();
         this.worldRegistry.free();
         this.mainRenderer.free();
         this.voxelizedBlockObserver.unregister();
         USED_BUFFERS.clear();
         INSTANCE = null;
      }
   }

   public static Optional<PhotonicsProperties> getProperties() {
      return Iris.getCurrentPack().map(e -> (ShaderPackAccessor)e).map(ShaderPackAccessor::getShaderProperties).map(e -> (PhotonicsProperties)e);
   }

   public static boolean isDisabled() {
      return INSTANCE == null;
   }

   public static boolean shouldBeEnabled() {
      if (!Iris.getIrisConfig().areShadersEnabled()) {
         return false;
      } else if (getAppliedPatch(true) != null) {
         boolean shaderPackSupported = Boolean.parseBoolean(PATCHED_SHADERPACK_PROPERTIES.getOrDefault("photonics.supported", false).toString());
         return !shaderPackSupported ? false : Boolean.parseBoolean(SHADERPACK_CHANGED_OPTIONS.getOrDefault("PHOTONICS_ENABLED", "true"));
      } else {
         return getProperties().map(PhotonicsProperties::isPhotonicsEnabled).map(e -> e.orElse(false)).orElse(false);
      }
   }

   public static void bindBuffers(int shaderId) {
      if (!isDisabled()) {
         INSTANCE.getMainRenderer().bindProgramBuffers(shaderId, USED_BUFFERS);
      }
   }

   public static void watchFolder(File folder, Runnable callback) {
      if (folder.exists()) {
         try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            WatchKey watchKey = folder.toPath().register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
            new Thread(() -> {
               while (true) {
                  if (!watchKey.pollEvents().isEmpty()) {
                     callback.run();
                  }

                  try {
                     Thread.sleep(100L);
                  } catch (InterruptedException e) {
                     throw new RuntimeException(e);
                  }
               }
            }).start();
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }
   }

   public MainRenderer getMainRenderer() {
      return this.mainRenderer;
   }

   public RenderDispatcher getRenderDispatcher() {
      return this.renderDispatcher;
   }

   public WorldRegistry getWorldRegistry() {
      return this.worldRegistry;
   }

   public BlockRegistry getBlockRegistry() {
      return this.worldRegistry.getBlockRegistry();
   }

   private static Optional<Patch> getPatch(String name, boolean enabled) {
      return AVAILABLE_PATCHES.stream().filter(p -> p.canBeApplied(name, enabled)).findFirst();
   }

   private static Patch getAppliedPatch(boolean enabled) {
      return SHADERPACK_PROPERTIES != null && SHADERPACK_PROPERTIES.containsKey("photonics.enabled")
         ? null
         : Iris.getIrisConfig().getShaderPackName().flatMap(e -> getPatch(e, enabled)).orElse(null);
   }

   public static Patch getAppliedPatch() {
      return getAppliedPatch(shouldBeEnabled());
   }

   private static Stream<Path> loadClassStream() throws URISyntaxException, IOException {
      URL resource = Patch.class.getClassLoader().getResource("assets/photonics/patches/");
      return resource == null ? Stream.empty() : Files.list(Path.of(resource.toURI()));
   }

   public static void reloadPatches() {
      for (FileSystem fs : FILE_SYSTEMS) {
         try {
            fs.close();
         } catch (IOException e) {
            Photonics.error("Error while closing patch file system", e);
         }
      }

      FILE_SYSTEMS.clear();
      AVAILABLE_PATCHES.clear();
      Path fsDevPath = DEV_ENV_SHADERS_PATH.resolve("patches");
      Path fsPath = SHADER_PATCHES_PATH;
      if (Files.notExists(fsPath)) {
         try {
            Files.createDirectory(fsPath);
         } catch (IOException e) {
            Photonics.error("Failed to create default patches directory", e);
         }
      }

      try (
         Stream<Path> fsDevStream = Files.exists(fsDevPath) ? Files.list(fsDevPath) : Stream.of();
         Stream<Path> fsStream = Files.exists(fsPath) ? Files.list(fsPath) : Stream.of();
         Stream<Path> classStream = loadClassStream();
      ) {
         Stream.of(fsStream, fsDevStream, classStream).flatMap(ex -> ex).filter(x$0 -> Files.exists(x$0)).map(p -> {
            String name = p.getFileName().toString();
            int extensionIndex = name.lastIndexOf(46);
            if (extensionIndex == -1) {
               return Pair.of(p, null);
            }

            String extension = name.substring(extensionIndex);
            if (!extension.equals(".zip")) {
               return Pair.of(p, null);
            }

            try {
               FileSystem fs = FileSystems.newFileSystem(p);
               return Pair.of(fs.getPath("./"), fs);
            } catch (IOException e) {
               return Pair.of(p, null);
            }
         }).forEach(p -> {
            try {
               if (p.second() != null) {
                  FILE_SYSTEMS.add((FileSystem)p.second());
               }

               AVAILABLE_PATCHES.add(Patch.of((Path)p.first(), true));
               AVAILABLE_PATCHES.add(Patch.of((Path)p.first(), false));
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
         });
      } catch (IOException | URISyntaxException e) {
         throw new RuntimeException(e);
      }
   }

   public static String readShaderFile(ShaderPackPath path, boolean patchFile) {
      String source = null;
      Patch patch = getAppliedPatch();
      if (patch != null && patchFile) {
         source = patch.readPatchedFile(path);
      }

      if (source == null) {
         try {
            label58: {
               if (!path.isPhotonicsPath()) {
                  return readShaderAndPreprocess(path);
               }

               String relativeToPhotonics = path.getRelativeToPhotonics();
               if (patch == null && !AUTO_REPLACED_FILES.contains(relativeToPhotonics)) {
                  Optional<String> content = tryReadFile(path);
                  if (content.isPresent()) {
                     source = content.get();
                     break label58;
                  }
               }

               Path devEnvShaderPath = DEV_ENV_SHADERS_PATH.resolve(relativeToPhotonics);
               if (Files.exists(devEnvShaderPath)) {
                  return readShaderAndPreprocess(new ShaderPackPath(devEnvShaderPath));
               }

               URL jarShaderUrl = IncludeGraph.class.getClassLoader().getResource("assets/photonics/shaders/" + relativeToPhotonics);
               if (jarShaderUrl == null) {
                  return null;
               }

               Path jarShaderPath = Path.of(jarShaderUrl.toURI());
               if (Files.exists(jarShaderPath)) {
                  return readShaderAndPreprocess(new ShaderPackPath(jarShaderPath));
               }
            }
         } catch (Exception var7) {
         }
      }

      if (source == null) {
         throw new IllegalStateException("Couldn't read file " + path);
      } else {
         return ShaderUtil.preprocessForward(source);
      }
   }

   private static String readShaderAndPreprocess(ShaderPackPath path) throws IOException {
      return ShaderUtil.preprocessForward(path.readFile());
   }

   private static Optional<String> tryReadFile(ShaderPackPath path) {
      try {
         return Optional.of(path.readFile());
      } catch (IOException e) {
         return Optional.empty();
      }
   }

   static {
      watchFolder(DEV_ENV_SHADERS_PATH.toFile(), () -> Minecraft.getInstance().execute(() -> {
         try {
            Iris.reload();
         } catch (IOException var1) {
         }
      }));
   }
}
