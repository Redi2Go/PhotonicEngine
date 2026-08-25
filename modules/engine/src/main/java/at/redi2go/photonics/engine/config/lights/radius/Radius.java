package at.redi2go.photonics.engine.config.lights.radius;

public record Radius(float value) implements LightRadius {
    @Override
    public float get() {
        return value;
    }
}
