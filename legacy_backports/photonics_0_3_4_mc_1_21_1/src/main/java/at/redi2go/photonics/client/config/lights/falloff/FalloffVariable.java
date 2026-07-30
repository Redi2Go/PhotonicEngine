package at.redi2go.photonics.client.config.lights.falloff;

import at.redi2go.photonics.client.config.Variable;

public class FalloffVariable extends Variable<LightFalloff> implements LightFalloff {
   protected FalloffVariable(String name) {
      super(name, LightFalloff.TYPE);
   }

   @Override
   public float get() {
      return this.actual().get();
   }
}
