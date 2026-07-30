package at.redi2go.photonics.client.config.lights.intensity;

import at.redi2go.photonics.client.config.Variable;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

public interface LightIntensity {
   Variable.Type<LightIntensity> TYPE = new Variable.Type<>("light_intensity");

   float get();

   class Adapter extends Variable.Adapter<LightIntensity, IntensityVariable> {
      protected LightIntensity readValue(JsonReader in) throws IOException {
         return new Intensity((float)in.nextDouble());
      }

      protected void writeValue(JsonWriter out, LightIntensity value) throws IOException {
         out.value(value.get());
      }

      protected IntensityVariable newVariable(String name) throws IOException {
         return new IntensityVariable(name);
      }
   }
}
