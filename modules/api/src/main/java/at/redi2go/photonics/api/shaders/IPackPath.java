package at.redi2go.photonics.api.shaders;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public interface IPackPath {
    Optional<IPackPath> parent();

    IPackPath resolve(String path);

    Path resolved(Path root);

    boolean startsWith(IPackPath path);

    default boolean startsWith(String absolutePath) {
        return startsWith(fromAbsolutePath(absolutePath));
    }

    String pathString();

    static IPackPath fromAbsolutePath(String absolutePath) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    class Adapter extends TypeAdapter<IPackPath> {

        @Override
        public void write(JsonWriter out, IPackPath value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public IPackPath read(JsonReader in) throws IOException {
            return IPackPath.fromAbsolutePath(in.nextString());
        }
    }
}
