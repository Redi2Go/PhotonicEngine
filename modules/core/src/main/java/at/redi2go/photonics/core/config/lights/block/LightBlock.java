package at.redi2go.photonics.core.config.lights.block;

import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.config.Variable;
import at.redi2go.photonics.core.config.lights.predicate.LightPredicate;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.io.IOException;
import java.util.List;

public interface LightBlock {
    Variable.Type<LightBlock> TYPE = new Variable.Type<>("light_block");

    List<LightPredicate> listPredicates() throws CommandSyntaxException;

    class Adapter extends TypeAdapter<LightBlock> {
        public static String PRIORITY_NAME = "priority";
        public static String VALUE_NAME = "value";

        @Override
        public void write(JsonWriter out, LightBlock lightBlock) throws IOException {
            if (lightBlock instanceof DefaultLightBlock obj) {
                out.value(obj.value());
            } else if (lightBlock instanceof LightBlockWithPriority obj) {
                out.beginObject();

                out.name(PRIORITY_NAME);
                out.value(obj.priority());

                out.name(VALUE_NAME);
                out.value(obj.value());

                out.endObject();
            } else {
                Photonics.LOGGER.warn("An unknown light block ({}) was serialized", lightBlock.getClass().getName());
                out.nullValue();
            }
        }

        @Override
        public LightBlock read(JsonReader in) throws IOException {
            switch(in.peek()) {
                case STRING -> {
                    final var str = in.nextString();
                    if (!str.isEmpty() && str.charAt(0) == Variable.VAR_CHAR)
                        return new LightBlockVariable(str.substring(1), LightPredicate.DEFAULT_PRIORITY);

                    return new DefaultLightBlock(str);
                }

                case BEGIN_OBJECT -> {
                    in.beginObject();

                    String value = null;
                    int priority = LightPredicate.DEFAULT_PRIORITY;

                    while (in.hasNext() && in.peek() != JsonToken.END_OBJECT) {
                        final String name = in.nextName();
                        switch (name) {
                            case "priority" -> priority = in.nextInt();

                            case "value" -> value = in.nextString();

                            default -> throw new IOException("unexpected name '" + name + "'");
                        }
                    }

                    in.endObject();

                    if (value == null)
                        throw new IOException("missing value for block");

                    if (!value.isEmpty() && value.charAt(0) == Variable.VAR_CHAR)
                        return new LightBlockVariable(value.substring(1), priority);

                    return new LightBlockWithPriority(value, priority);
                }

                default -> throw new IOException("unexpected token " + in.peek());
            }
        }
    }
}
