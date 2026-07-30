package at.redi2go.photonics.client.config.lights;

import at.redi2go.photonics.client.config.Variable;
import at.redi2go.photonics.client.config.lights.color.LightColor;
import at.redi2go.photonics.client.config.lights.falloff.LightFalloff;
import at.redi2go.photonics.client.config.lights.intensity.LightIntensity;
import at.redi2go.photonics.client.config.lights.radius.LightRadius;
import java.util.LinkedHashMap;
import java.util.Optional;

public class LightDefines {
   public static final LightDefines EMPTY = new LightDefines(null);
   LinkedHashMap<String, LightColor> colors;
   LinkedHashMap<String, LightIntensity> intensities;
   LinkedHashMap<String, LightRadius> radii;
   LinkedHashMap<String, LightFalloff> falloffs;

   private LightDefines(Void unused) {
      LinkedHashMap temp = new LinkedHashMap(0);
      this.colors = temp;
      this.intensities = temp;
      this.radii = temp;
      this.falloffs = temp;
   }

   public LightDefines() {
      this.colors = new LinkedHashMap<>();
      this.intensities = new LinkedHashMap<>();
      this.radii = new LinkedHashMap<>();
      this.falloffs = new LinkedHashMap<>();
   }

   private void setOwner(LinkedHashMap<String, ?> map, Variable.Owner owner) {
      for (Object value : map.values()) {
         if (value instanceof Variable<?> variable) {
            variable.setOwner(owner);
         }
      }
   }

   public void setOwner(Variable.Owner owner) {
      if (this == EMPTY) {
         this.colors.clear();
      } else {
         this.setOwner(this.colors, owner);
         this.setOwner(this.intensities, owner);
         this.setOwner(this.radii, owner);
         this.setOwner(this.falloffs, owner);
      }
   }

   public <T> Optional<T> getValue(Variable.Type<T> type, String name) {
      if (type == LightColor.TYPE) {
         return Optional.ofNullable((T)this.colors.get(name));
      } else if (type == LightIntensity.TYPE) {
         return Optional.ofNullable((T)this.intensities.get(name));
      } else if (type == LightRadius.TYPE) {
         return Optional.ofNullable((T)this.radii.get(name));
      } else {
         return type == LightFalloff.TYPE ? Optional.ofNullable((T)this.falloffs.get(name)) : Optional.empty();
      }
   }

   public class Owner implements Variable.Owner {
      @Override
      public int mod() {
         int hash = System.identityHashCode(LightDefines.this);
         return hash == -1 ? System.identityHashCode(this) : hash;
      }

      @Override
      public <T> Optional<T> getValue(Variable.Type<T> type, String name) {
         return LightDefines.this.getValue(type, name);
      }
   }
}
