package at.redi2go.photonics.client.config.lights.intensity;

import at.redi2go.photonics.client.config.Variable;

public class IntensityVariable extends Variable<LightIntensity> implements LightIntensity {
   protected IntensityVariable(String name) {
      super(name, LightIntensity.TYPE);
   }

   @Override
   public float get() {
      return this.actual().get();
   }
}
