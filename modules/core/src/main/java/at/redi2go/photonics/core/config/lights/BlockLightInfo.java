package at.redi2go.photonics.core.config.lights;

import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlock;
import at.redi2go.photonics.api.mc.world.level.ILevelReader;
import at.redi2go.photonics.core.config.lights.color.LightColor;
import at.redi2go.photonics.core.config.lights.predicate.LightPredicate;
import org.jetbrains.annotations.NonNls;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Objects;

public final class BlockLightInfo implements Comparable<BlockLightInfo> {
    public static final Vector3f LUMINANCE_COEF = new Vector3f(0.2126F, 0.7152F, 0.0722F);

    private final @NonNls LightPredicate predicate;
    private final @NonNls LightColor color;
    private final float intensity;  // Intensity is multiplied by 100 to make it more intuitive to edit, see adjustedIntensity
    private final float radius; // This value is 'reversed' compared to the value used by Photonics so its more intuitive to edit, see radiusRcp
    private final float falloff;
    private final boolean isTraced;
    private final boolean requestedTrace;

    private final float adjustedIntensity;
    private final float luminanceDotColor;
    private final float radiusRcp;
    private final float blockRadius;

    public BlockLightInfo(
            @NonNls LightPredicate predicate,
            @NonNls LightColor color,
            float intensity,
            float radius,
            float falloff,
            boolean isTraced,
            boolean requestedTrace
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

        this.adjustedIntensity = intensity / 100f;
        this.luminanceDotColor = getColorAsVector().dot(LUMINANCE_COEF);
        this.radiusRcp = 1 / radius;

        //0.001 is the minimum amount of light
        this.blockRadius = getBlockRadius(getColorAsVector(), new Vector2f(0.9f,radiusRcp), falloff);
    }

    public IBlock block() {
        return predicate.block();
    }

    public float intensity() {
        return intensity;
    }

    public float radius() {
        return radius;
    }

    public float falloff() {
        return falloff;
    }

    public boolean isTraced() {
        return isTraced;
    }

    public boolean requestedTrace() {
        return requestedTrace;
    }

    public float radiusInBlocks() {
        return blockRadius;
    }

    public boolean emitsLight(IBlockPos pos, ILevelReader level) {
        // The block should already be loaded
        return predicate.test(pos, level);
    }

    public float luminanceFrom(Vector3d lightPosition, Vector3d samplePosition) {
        var result = samplePosition.sub(lightPosition, new Vector3d());
        float distanceSquared = (float) (result.dot(result) * falloff);

        return this.luminanceDotColor / (0.9f + distanceSquared * radiusRcp);
    }

    public Vector3f getColorAsVector() {
        return color.toVec3f().mul(adjustedIntensity);
    }

    public Vector2f getAttenuationAsVector() {
        return new Vector2f(0.9f, radiusRcp);
    }

    public Vector4f[] toVector4Array(Vector3f position, int blockId) {
        return new Vector4f[]{
                new Vector4f(position, Float.intBitsToFloat(blockId)),
                new Vector4f(getColorAsVector(), intensity / 100f),
                new Vector4f(getAttenuationAsVector(), falloff(), radiusInBlocks())
        };
    }

    public Matrix4fc toMatrix4(Vector3f position, int blockId) {
        var arrayData = toVector4Array(position, blockId);

        return new Matrix4f(
            arrayData[0],
            arrayData[1],
            arrayData[2],
            new Vector4f(0f)
        );
    }

    @Override
    public int compareTo(@NonNls BlockLightInfo o) {
        return predicate.compareTo(o.predicate);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        return obj instanceof BlockLightInfo other &&
                Float.compare(intensity, other.intensity) == 0 &&
                Float.compare(radius, other.radius) == 0 &&
                Float.compare(falloff, other.falloff) == 0
                && isTraced == other.isTraced
                && requestedTrace == other.requestedTrace
                && color.equals(other.color);
    }

    @Override
    public int hashCode() {
        int result = color.hashCode();

        result = 31 * result + Float.hashCode(intensity);
        result = 31 * result + Float.hashCode(radius);
        result = 31 * result + Float.hashCode(falloff);
        result = 31 * result + Boolean.hashCode(isTraced);
        result = 31 * result + Boolean.hashCode(requestedTrace);

        return result;
    }

    @Override
    public String toString() {
        return "BlockLightInfo{" +
                "predicate=" + predicate +
                ", color=" + color +
                ", intensity=" + intensity +
                ", radius=" + radius +
                ", falloff=" + falloff +
                ", isTraced=" + isTraced +
                ", requestedTrace=" + requestedTrace +
                '}';
    }

    public static float getBlockRadius(Vector3f color, Vector2f attenuation, float falloff) {
        return (float) Math.sqrt(((((color.dot(LUMINANCE_COEF)) / 0.001f) - attenuation.x) / attenuation.y) / falloff);
    }
}
