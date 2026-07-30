package at.redi2go.photonics.client.config;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

public abstract class Variable<U> {
   public static final char VAR_CHAR = '*';
   private final String name;
   private final Variable.Type<U> type;
   private int mod = -1;
   @Nullable
   private U value;
   @Nullable
   private Variable.Owner owner;

   protected Variable(@NonNls String name, @NonNls Variable.Type<U> type) {
      this.name = Objects.requireNonNull(name, "name was null");
      this.type = Objects.requireNonNull(type, "type was null");
   }

   public String name() {
      return this.name;
   }

   public void setOwner(@NonNls Variable.Owner owner) {
      if (this.owner != null && this.owner != owner) {
         throw new IllegalStateException("variable '" + this.name + "' already has an owner");
      }

      this.owner = owner;
   }

   protected U actual() {
      Variable.Owner owner = Objects.requireNonNull(this.owner, "owner was null for " + this.name);
      int latestMod = owner.mod();
      if (this.mod == latestMod && this.value != null) {
         return this.value;
      }

      Optional<U> result = owner.getValue(this.type, this.name);
      if (result.isEmpty()) {
         throw new IllegalArgumentException("Could not find " + this.type.name + " with name '" + this.name + "'");
      }

      this.mod = latestMod;
      this.value = result.get();
      return this.value;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj instanceof Variable<?> var) {
         return !obj.getClass().equals(this.getClass()) ? false : this.name.equals(var.name);
      } else {
         return this.actual().equals(obj);
      }
   }

   @Override
   public int hashCode() {
      return this.actual().hashCode();
   }

   @Override
   public String toString() {
      return "*" + this.name;
   }

   public abstract static class Adapter<U, T extends Variable<? extends U>> extends TypeAdapter<U> {
      protected abstract U readValue(JsonReader var1) throws IOException;

      protected abstract void writeValue(JsonWriter var1, U var2) throws IOException;

      protected abstract T newVariable(String var1) throws IOException;

      protected U fromString(String str) throws IOException {
         throw new IOException("cannot read value from string");
      }

      public U read(JsonReader in) throws IOException {
         switch (in.peek()) {
            case NULL:
               return null;
            case STRING:
               String str = in.nextString();
               if (!str.isEmpty() && str.charAt(0) == '*') {
                  return (U)this.newVariable(str.substring(1));
               }

               return this.fromString(str);
            default:
               return this.readValue(in);
         }
      }

      public void write(JsonWriter out, U value) throws IOException {
         if (value instanceof Variable<?> var) {
            out.value("*" + var.name());
         } else if (value == null) {
            out.nullValue();
         } else {
            this.writeValue(out, value);
         }
      }
   }

   public interface Owner {
      int mod();

      <T> Optional<T> getValue(@NonNls Variable.Type<T> var1, @NonNls String var2);
   }

   public abstract static class StrAdapter<U, T extends Variable<? extends U>> extends Variable.Adapter<U, T> {
      @Override
      protected abstract U fromString(String var1) throws IOException;

      protected abstract String toString(U var1) throws IOException;

      @Override
      protected U readValue(JsonReader in) throws IOException {
         return this.fromString(in.nextString());
      }

      @Override
      protected void writeValue(JsonWriter out, U value) throws IOException {
         out.value(this.toString(value));
      }
   }

   public record Type<U>(String name) {
   }
}
