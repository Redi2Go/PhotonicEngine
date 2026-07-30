package at.redi2go.photonics.client.config.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class BlockAdapter extends TypeAdapter<Block> {
   public void write(JsonWriter out, Block value) throws IOException {
      out.value(toString(value));
   }

   public Block read(JsonReader in) throws IOException {
      return fromString(in.nextString());
   }

   public static String toString(Block block) {
      return BuiltInRegistries.BLOCK.getKey(block).toString();
   }

   public static Block fromString(String str) {
      return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(str));
   }
}
