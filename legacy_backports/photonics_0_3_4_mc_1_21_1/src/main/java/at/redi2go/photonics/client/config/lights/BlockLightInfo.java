package at.redi2go.photonics.client.config.lights;

import at.redi2go.photonics.client.config.lights.color.LightColor;
import at.redi2go.photonics.client.config.lights.predicate.LightPredicate;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jetbrains.annotations.NonNls;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jetbrains.annotations.NotNull;

public final class BlockLightInfo implements Comparable<BlockLightInfo> {
   public static final Vector3f LUMINANCE_COEF = new Vector3f(0.2126F, 0.7152F, 0.0722F);
   @NonNls
   private final LightPredicate predicate;
   @NonNls
   private final LightColor color;
   private final float intensity;
   private final float radius;
   private final float falloff;
   private final boolean isTraced;
   private final boolean requestedTrace;
   private final float adjustedIntensity;
   private final float luminanceDotColor;
   private final float radiusRcp;
   private final float blockRadius;

   public BlockLightInfo(
      @NonNls LightPredicate predicate, @NonNls LightColor color, float intensity, float radius, float falloff, boolean isTraced, boolean requestedTrace
   ) {
      Objects.requireNonNull(predicate, "predicate was null");
      Objects.requireNonNull(color, "color was null");
      this.predicate = predicate;
      this.color = color;
      this.intensity = intensity;
      this.radius = radius;
      this.falloff = falloff;
      this.isTraced = isTraced;
      this.requestedTrace = requestedTrace;
      this.adjustedIntensity = intensity / 100.0F;
      this.luminanceDotColor = this.getColorAsVector().dot(LUMINANCE_COEF);
      this.radiusRcp = 1.0F / radius;
      this.blockRadius = getBlockRadius(this.getColorAsVector(), new Vector2f(0.9F, this.radiusRcp), falloff);
   }

   public Block block() {
      return this.predicate.block();
   }

   public float intensity() {
      return this.intensity;
   }

   public float radius() {
      return this.radius;
   }

   public float falloff() {
      return this.falloff;
   }

   public boolean isTraced() {
      return this.isTraced;
   }

   public boolean requestedTrace() {
      return this.requestedTrace;
   }

   public float radiusInBlocks() {
      return this.blockRadius;
   }

   public boolean emitsLight(BlockPos pos, LevelReader level) {
      return this.predicate.test(new BlockInWorld(level, pos, false));
   }

   public float luminanceFrom(Vector3f lightPosition, Vector3f samplePosition) {
      float dx = samplePosition.x - lightPosition.x;
      float dy = samplePosition.y - lightPosition.y;
      float dz = samplePosition.z - lightPosition.z;
      float distanceSquared = (dx * dx + dy * dy + dz * dz) * this.falloff;
      return this.luminanceDotColor / (0.9F + distanceSquared * this.radiusRcp);
   }

   public Vector3f getColorAsVector() {
      return this.color.toVec3f().mul(this.adjustedIntensity);
   }

   public Vector2f getAttenuationAsVector() {
      return new Vector2f(0.9F, this.radiusRcp);
   }

   public Vector4f[] toVector4Array(Vector3f position, int blockId) {
      return new Vector4f[]{
         new Vector4f(position, Float.intBitsToFloat(blockId)),
         new Vector4f(this.getColorAsVector(), this.intensity / 100.0F),
         new Vector4f(this.getAttenuationAsVector(), this.falloff(), this.radiusInBlocks())
      };
   }

   public int compareTo(@NotNull BlockLightInfo o) {
      return this.predicate.compareTo(o.predicate);
   }

   @Override
   public String toString() {
      return "BlockLightInfo{predicate="
         + this.predicate
         + ", color="
         + this.color
         + ", intensity="
         + this.intensity
         + ", radius="
         + this.radius
         + ", falloff="
         + this.falloff
         + ", isTraced="
         + this.isTraced
         + ", requestedTrace="
         + this.requestedTrace
         + "}";
   }

   public static float getBlockRadius(Vector3f color, Vector2f attenuation, float falloff) {
      return (float)Math.sqrt((color.dot(LUMINANCE_COEF) / 0.001F - attenuation.x) / attenuation.y / falloff);
   }
}
