package at.redi2go.photonics.core.rendering.world.block.palette;

import at.redi2go.photonics.core.rendering.world.block.TextureData;
import at.redi2go.photonics.core.rendering.world.block.VoxelColor;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public final class MutablePaletteEntry extends PaletteEntry {
    int index = 0;
    int tint = -1;

    Int2IntMap tintDependencies;

    public MutablePaletteEntry() {
        super();
    }

    public MutablePaletteEntry(PaletteEntry other) {
        super();
        copyFrom(other);
    }

    public MutablePaletteEntry(PaletteEntry other, int tint) {
        super();
        copyFrom(other);

        this.tint = tint;
    }

    public void makePaletteWhole() {
        TextureData notNullFace = null;
        for (int i = 0; i < 6; i++) {
            var face = faces[i];
            if (face == null) continue;

            if (notNullFace == null)
                notNullFace = face;
            else if (face.gt(notNullFace))
                notNullFace = face;
        }

        for (int i = 0; i < 6; i++) {
            if (faces[i] != null) continue;

            faces[i] = notNullFace;
        }
    }

    public int getIndex(int tint) {
        if (this.tint == tint) return index;

        return tintDependencies.get(tint);
    }

    public void addTint(int tint) {
        if (this.tint == tint) return;

        if (tintDependencies == null) tintDependencies = new Int2IntOpenHashMap();
        tintDependencies.put(tint, 0);
    }

    public boolean update(int normal, int tint, TextureData data) {
        this.tint = tint;
        faces[normal] = data;
        hasTransparent = hasTransparent || VoxelColor.a(data.color()) != 255;

        return true;
    }

    public void update(int tint, PaletteEntry entry) {
        this.tint = tint;

        for (int i = 0; i < 6; i++) {
            var newFace = entry.faces[i];
            if (newFace == null) continue;

            faces[i] = newFace;
        }

        hasTransparent |= entry.hasTransparent;
    }
}