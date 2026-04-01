package at.redi2go.photonics.core.config.lights.falloff;

import at.redi2go.photonics.core.config.Variable;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public interface LightFalloff {
    Variable.Type<LightFalloff> TYPE = new Variable.Type<>("light_falloff");

    float get();

    class Adapter extends Variable.Adapter<LightFalloff, FalloffVariable> {
        @Override
        protected LightFalloff readValue(JsonReader in) throws IOException {
            return new Falloff((float) in.nextDouble());
        }

        @Override
        protected void writeValue(JsonWriter out, LightFalloff value) throws IOException {
            out.value(value.get());
        }

        @Override
        protected FalloffVariable newVariable(String name) throws IOException {
            return new FalloffVariable(name);
        }
    }
}
