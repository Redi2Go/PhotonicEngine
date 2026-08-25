package at.redi2go.photonics.engine.config.lights.intensity;

public record Intensity(float value) implements LightIntensity {
    @Override
    public float get() {
        return value;
    }
}
