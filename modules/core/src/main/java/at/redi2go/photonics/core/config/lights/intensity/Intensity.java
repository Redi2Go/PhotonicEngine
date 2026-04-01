package at.redi2go.photonics.core.config.lights.intensity;

public record Intensity(float value) implements LightIntensity {
    @Override
    public float get() {
        return value;
    }
}
