package at.redi2go.photonics.core.config;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/**
 * A reference to a named value of {@link U}.
 *
 * @param <U> The type of the variable
 */
public abstract class Variable<@NonNls U> {
    /**
     * The character that indicates a variable reference
     */
    public final static char VAR_CHAR = '*';

    /**
     * The name of the referenced value
     */
    private final String name;

    /**
     * The type of the variable.
     */
    private final Type<U> type;

    /**
     * An increment integer that tracks changes to {@link #owner}
     */
    private int mod = -1;

    /**
     * The last known value of {@link #name}
     */
    private @Nullable U value;

    /**
     * The owner of this variable that supplies {@link #mod} and {@link #value}
     */
    private @Nullable Owner owner;

    protected Variable(
            @NonNls String name,
            @NonNls Type<U> type
    ) {
        this.name = Objects.requireNonNull(name, "name was null");
        this.type = Objects.requireNonNull(type, "type was null");
    }

    public String name() {
        return name;
    }

    public void setOwner(@NonNls Owner owner) {
        if (this.owner != null && this.owner != owner)
            throw new IllegalStateException("variable '" + name + "' already has an owner");

        this.owner = owner;
    }

    /**
     * Returns the value of {@link #name}, updating it if {@link #mod} differs from {@link #owner}.
     */
    protected U actual() {
        final Owner owner = Objects.requireNonNull(this.owner, "owner was null for " + name);
        final int latestMod = owner.mod();

        if (mod == latestMod && value != null) return value;

        final Optional<U> result = owner.getValue(type, name);
        if (result.isEmpty())
            throw new IllegalArgumentException("Could not find " + type.name + " with name '" + name + "'");

        mod = latestMod;
        value = result.get();

        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Variable<?> var)) return actual().equals(obj);
        if (!obj.getClass().equals(this.getClass())) return false;

        return name.equals(var.name);
    }

    @Override
    public int hashCode() {
        return actual().hashCode();
    }

    @Override
    public String toString() {
        return VAR_CHAR + name;
    }

    public record Type<@NonNls U>(String name) { }

    public interface Owner {
        /**
         * An integer that increments every time this changes.
         */
        int mod();

        <@NonNls T> Optional<T> getValue(
                @NonNls Type<T> type,
                @NonNls String name
        );
    }

    public static abstract class Adapter<@NonNls U, T extends Variable<? extends U>> extends TypeAdapter<U> {
        protected abstract U readValue(JsonReader in) throws IOException;

        protected abstract void writeValue(JsonWriter out, U value) throws IOException;

        protected abstract T newVariable(String name) throws IOException;

        /**
         * Fallback case in case a string was not a variable.
         *
         * @see #VAR_CHAR
         */
        protected U fromString(String str) throws IOException {
            throw new IOException("cannot read value from string");
        }

        @Override
        @SuppressWarnings("unchecked")
        public U read(JsonReader in) throws IOException {
            switch (in.peek()) {
                case NULL -> { return null; }

                case STRING -> {
                    String str = in.nextString();
                    if (str.isEmpty() || str.charAt(0) != VAR_CHAR)
                        return fromString(str);

                    return (U) newVariable(str.substring(1));
                }

                default -> { return readValue(in); }
            }
        }

        @Override
        public void write(JsonWriter out, U value) throws IOException {
            if (value instanceof Variable<?> var) {
                out.value(VAR_CHAR + var.name());
                return;
            }

            if (value == null) {
                out.nullValue();
                return;
            }

            writeValue(out, value);
        }
    }

    public static abstract class StrAdapter<@NonNls U, T extends Variable<? extends U>> extends Adapter<U, T> {
        @Override
        protected abstract U fromString(String str) throws IOException;

        protected abstract String toString(U value) throws IOException;

        @Override
        protected U readValue(JsonReader in) throws IOException {
            return fromString(in.nextString());
        }

        @Override
        protected void writeValue(JsonWriter out, U value) throws IOException {
            out.value(toString(value));
        }
    }
}

