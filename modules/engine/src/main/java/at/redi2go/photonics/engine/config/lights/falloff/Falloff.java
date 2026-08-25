package at.redi2go.photonics.engine.config.lights.falloff;

public record Falloff(float value) implements LightFalloff {
    @Override
    public float get() {
        return value;
    }
}
