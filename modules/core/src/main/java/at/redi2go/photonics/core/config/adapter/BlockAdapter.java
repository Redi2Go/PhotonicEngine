package at.redi2go.photonics.core.config.adapter;

import at.redi2go.photonics.game.minecraft.IIdentifier;
import at.redi2go.photonics.game.minecraft.world.level.IBlock;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class BlockAdapter extends TypeAdapter<IBlock> {
    @Override
    public void write(JsonWriter out, IBlock value) throws IOException {
        out.value(value.ph$id().toString());
    }

    @Override
    public IBlock read(JsonReader in) throws IOException {
        return IBlock.fromIdOrThrow(IIdentifier.parse(in.nextString()));
    }
}
