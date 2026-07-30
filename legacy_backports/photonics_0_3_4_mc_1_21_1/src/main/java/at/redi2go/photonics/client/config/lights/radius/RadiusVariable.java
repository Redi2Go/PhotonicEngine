package at.redi2go.photonics.client.config.lights.radius;

import at.redi2go.photonics.client.config.Variable;

public class RadiusVariable extends Variable<LightRadius> implements LightRadius {
   protected RadiusVariable(String name) {
      super(name, LightRadius.TYPE);
   }

   @Override
   public float get() {
      return this.actual().get();
   }
}
