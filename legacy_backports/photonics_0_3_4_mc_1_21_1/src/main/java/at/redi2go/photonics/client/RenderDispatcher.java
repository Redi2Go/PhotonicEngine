package at.redi2go.photonics.client;

import at.redi2go.photonics.client.api.PhotonicsProperties;
import at.redi2go.photonics.client.config.PhotonicsConfig;
import at.redi2go.photonics.client.config.lights.BlockLightInfo;
import at.redi2go.photonics.client.mixin.InventoryAccessor;
import at.redi2go.photonics.client.rendering.MinecraftAccessor;
import at.redi2go.photonics.client.rendering.opengl.objects.AtomicIntegerImage;
import at.redi2go.photonics.client.rendering.opengl.objects.Destructable;
import at.redi2go.photonics.client.rendering.opengl.objects.TextureObject;
import at.redi2go.photonics.client.rendering.opengl.rendering.IRenderDispatcher;
import at.redi2go.photonics.client.rendering.util.IrisUtil;
import at.redi2go.photonics.client.rendering.world.position.PChunkPos;
import com.mojang.blaze3d.platform.Window;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import kroppeb.stareval.function.FunctionReturn;
import kroppeb.stareval.function.Type;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.parsing.MatrixType;
import net.irisshaders.iris.parsing.VectorType;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.irisshaders.iris.uniforms.custom.cached.CachedUniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class RenderDispatcher implements IRenderDispatcher, Destructable {
   private static final Minecraft MC_INSTANCE = Minecraft.getInstance();
   private static final Vector3f ENCHANTMENT_GLINT_COLOR = new Vector3f(0.12941177F, 0.050980393F, 0.30980393F);
   private static final Vector2f ENCHANTMENT_GLINT_ATTENUATION = new Vector2f(0.9F, 0.9F);
   private static final float ENCHANTMENT_GLINT_FALLOFF = 0.75F;
   private final AtomicIntegerImage[] gi;
   private final Map<Integer, Integer> boundTextures = new HashMap<>();
   private final float enchantmentGlintStrength;
   private final Vector3f enchantmentGlintColor;
   private final float enchantmentGlintRadius;
   private final RenderDispatcher.HandheldLightCache mainHand = new RenderDispatcher.HandheldLightCache("main");
   private final RenderDispatcher.HandheldLightCache offHand = new RenderDispatcher.HandheldLightCache("off");

   public RenderDispatcher(float renderScale, PhotonicsProperties properties) {
      this.gi = IntStream.range(0, 5).mapToObj(i -> new AtomicIntegerImage(() -> {
         Window window = Minecraft.getInstance().getWindow();
         return new Vector3f(window.getWidth() * renderScale, window.getHeight() * renderScale, 2.0F);
      })).toArray(AtomicIntegerImage[]::new);
      this.enchantmentGlintStrength = properties.getEnchantmentGlintStrength();
      this.enchantmentGlintColor = ENCHANTMENT_GLINT_COLOR.mul(this.enchantmentGlintStrength, new Vector3f());
      this.enchantmentGlintRadius = BlockLightInfo.getBlockRadius(this.enchantmentGlintColor, ENCHANTMENT_GLINT_ATTENUATION, 0.75F);
   }

   @Override
   public TextureObject getTextureObject(String textureType) {
      return switch (textureType) {
         case "gi_x" -> this.gi[0];
         case "gi_y" -> this.gi[1];
         case "gi_z" -> this.gi[2];
         case "gi_w" -> this.gi[3];
         case "gi_d" -> this.gi[4];
         default -> throw new UnsupportedOperationException();
      };
   }

   @Override
   public void onChunkLoad() {
   }

   @Override
   public Set<PChunkPos> getInboundChunks() {
      Set<PChunkPos> chunks = new HashSet<>();
      Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
      if (cameraEntity == null) {
         return Set.of();
      }

      Vec3 chunkPos = cameraEntity.position().multiply(0.0625, 0.0625, 0.0625);
      int renderRadius = (Integer)Minecraft.getInstance().options.renderDistance().get() + 1;

      for (int x = -renderRadius; x < renderRadius; x++) {
         for (int y = -renderRadius; y < renderRadius; y++) {
            for (int z = -renderRadius; z < renderRadius; z++) {
               PChunkPos worldChunkPos = new PChunkPos((int)(chunkPos.x + x), (int)(chunkPos.y + y), (int)(chunkPos.z + z));
               chunks.add(worldChunkPos);
            }
         }
      }

      return chunks;
   }

   @Override
   public boolean isChunkEmpty(PChunkPos pos) {
      Level level = MC_INSTANCE.level;
      if (level == null) {
         return true;
      }

      if (level.isOutsideBuildHeight(16 * pos.y)) {
         return true;
      }

      LevelChunkSection[] sections = level.getChunk(new BlockPos(16 * pos.x, 16 * pos.y, 16 * pos.z)).getSections();
      int posY = pos.y - level.getMinBuildHeight() / 16;
      return sections[posY] == null || sections[posY].hasOnlyAir();
   }

   public Matrix4f getModelViewMatrix(Vector3f cameraPosition) {
      Matrix4f matrix4f = new Matrix4f(CapturedRenderingState.INSTANCE.getGbufferModelView());
      return matrix4f.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
   }

   @Override
   public Matrix4f getModelViewProjectionMatrix(Vector3f cameraPosition) {
      return new Matrix4f(CapturedRenderingState.INSTANCE.getGbufferProjection()).mul(this.getModelViewMatrix(cameraPosition));
   }

   @Override
   public boolean isLeftHanded() {
      return Minecraft.getInstance().options.mainHand().get() == HumanoidArm.LEFT;
   }

   public Object getValueUniform(String uniformName) {
      WorldRenderingPipeline worldRenderingPipeline = Iris.getPipelineManager().getPipelineNullable();
      if (!(worldRenderingPipeline instanceof IrisRenderingPipeline)) {
         return null;
      }

      CustomUniforms customUniforms = ((IrisRenderingPipeline)worldRenderingPipeline).getCustomUniforms();

      CachedUniform cachedUniform;
      try {
         cachedUniform = (CachedUniform)customUniforms.getVariable(uniformName);
      } catch (RuntimeException e) {
         return null;
      }

      if (cachedUniform == null) {
         return null;
      } else {
         cachedUniform.update();
         FunctionReturn functionReturn = new FunctionReturn();
         cachedUniform.writeTo(functionReturn);
         if (cachedUniform.getType() == Type.Float) {
            return functionReturn.floatReturn;
         } else if (cachedUniform.getType() == Type.Int) {
            return functionReturn.intReturn;
         } else if (cachedUniform.getType() == VectorType.VEC3) {
            Vector3f vector3f = (Vector3f)functionReturn.objectReturn;
            return new Vector3f(vector3f.x, vector3f.y, vector3f.z);
         } else if (cachedUniform.getType() == MatrixType.MAT4) {
            return functionReturn.objectReturn;
         } else {
            throw new IllegalArgumentException();
         }
      }
   }

   private static Vector3f getCameraPos() {
      Vector3d pos = MinecraftAccessor.getCameraPosition();
      return new Vector3f((float)pos.x, (float)pos.y, (float)pos.z);
   }

   private Vector4f[] createEmptyLight() {
      return new Vector4f[]{
         new Vector4f(getCameraPos(), Float.intBitsToFloat(-1)), new Vector4f(new Vector3f(0.0F), 1.0F), new Vector4f(0.9F, 0.9F, 1.0F, 0.0F)
      };
   }

   private Vector4f[] createEnchantedItemLight(ItemStack itemStack) {
      return !itemStack.isEnchanted()
         ? this.createEmptyLight()
         : new Vector4f[]{
            new Vector4f(getCameraPos(), Float.intBitsToFloat(-1)),
            new Vector4f(this.enchantmentGlintColor, this.enchantmentGlintStrength),
            new Vector4f(ENCHANTMENT_GLINT_ATTENUATION, 0.75F, this.enchantmentGlintRadius)
         };
   }

   private Vector4f[] createLightFromItem(ItemStack stack) {
      if (stack == null) {
         return this.createEmptyLight();
      }

      ResourceKey<Item> itemRegistryKey = (ResourceKey<Item>)BuiltInRegistries.ITEM.getResourceKey(stack.getItem()).orElse(null);
      if (itemRegistryKey == null) {
         return this.createEnchantedItemLight(stack);
      }

      Block block = BuiltInRegistries.BLOCK.getOptional(itemRegistryKey.location()).orElse(null);
      if (block == null) {
         return this.createEnchantedItemLight(stack);
      }

      BlockLightInfo light = PhotonicsConfig.getLightList().getDefault(block);
      return light == null ? this.createEnchantedItemLight(stack) : light.toVector4Array(getCameraPos(), IrisUtil.getBlockId(block.defaultBlockState()));
   }

   @Override
   public boolean hasMainHandLight() {
      this.mainHand.updateFromSlot(EquipmentSlot.MAINHAND);
      return !this.mainHand.isEmpty();
   }

   @Override
   public Vector4f[] getMainHandLight() {
      this.mainHand.updateFromSlot(EquipmentSlot.MAINHAND);
      return this.mainHand.getLight();
   }

   @Override
   public boolean hasOffhandLight() {
      this.offHand.updateFromSlot(EquipmentSlot.OFFHAND);
      return !this.offHand.isEmpty();
   }

   @Override
   public Vector4f[] getOffHandLight() {
      this.offHand.updateFromSlot(EquipmentSlot.OFFHAND);
      return this.offHand.getLight();
   }

   @Override
   public void free() {
      for (AtomicIntegerImage image : this.gi) {
         image.free();
      }
   }

   private class HandheldLightCache {
      private final String hand;
      private ItemStack stack;
      private Vector4f[] light = new Vector4f[]{new Vector4f(0.0F), new Vector4f(0.0F), new Vector4f(0.0F)};

      public HandheldLightCache(String hand) {
         this.hand = hand;
      }

      public boolean isEmpty() {
         return this.stack == null || this.light[1].x == 0.0F && this.light[1].y == 0.0F && this.light[1].z == 0.0F;
      }

      public Vector4f[] getLight() {
         return this.light;
      }

      private void updatePosition() {
         if (!this.isEmpty()) {
            Vector3f cameraPos = RenderDispatcher.getCameraPos();
            this.light[0].x = cameraPos.x;
            this.light[0].y = cameraPos.y;
            this.light[0].z = cameraPos.z;
         }
      }

      public void updateWith(ItemStack item) {
         if (item == this.stack) {
            this.updatePosition();
         } else {
            this.stack = item;
            this.light = RenderDispatcher.this.createLightFromItem(item);
            this.updatePosition();
            String itemId = item == null ? "none" : BuiltInRegistries.ITEM.getKey(item.getItem()).toString();
            Photonics.info(
               "Handheld light diagnostics: hand={}, item={}, active={}, color=({}, {}, {}), intensity={}, falloff={}, radius={}",
               this.hand,
               itemId,
               !this.isEmpty(),
               this.light[1].x,
               this.light[1].y,
               this.light[1].z,
               this.light[1].w,
               this.light[2].z,
               this.light[2].w
            );
         }
      }

      public void updateFromSlot(EquipmentSlot slot) {
         LocalPlayer player = RenderDispatcher.MC_INSTANCE.player;
         if (player == null) {
            this.updateWith(null);
         } else {
            Inventory inventory = player.getInventory();
            this.updateWith(inventory.player.getItemBySlot(slot));
         }
      }
   }
}
