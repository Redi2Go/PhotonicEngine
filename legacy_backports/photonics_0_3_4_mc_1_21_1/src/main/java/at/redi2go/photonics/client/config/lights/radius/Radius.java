package at.redi2go.photonics.client.config.lights.radius;

public record Radius(float value) implements LightRadius {
   @Override
   public float get() {
      return this.value;
   }
}
