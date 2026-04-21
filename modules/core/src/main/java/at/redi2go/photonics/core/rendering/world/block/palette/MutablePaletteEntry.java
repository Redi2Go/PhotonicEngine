package at.redi2go.photonics.core.rendering.world.block.palette;

import at.redi2go.photonics.core.rendering.world.block.VoxelColor;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public final class MutablePaletteEntry extends PaletteEntry {
    int index = 0;

    int tint = -1;
    MutablePaletteEntry mergedEntry = null;

    Int2IntMap tintDependencies;

    public MutablePaletteEntry() {
        super();
    }

    public MutablePaletteEntry(PaletteEntry other) {
        super();
        copyFrom(other);
    }

    void computeBuilderHashCode() {
        hashCode = tint;

        computeHashCodeImpl();
    }

    public int getIndex(int tint) {
        if (this.tint == tint) return index;

        return tintDependencies.get(tint);
    }

    public MutablePaletteEntry actualize() {
        var entry = this;
        while (true) {
            var nextEntry = entry.mergedEntry;
            if (nextEntry == null) break;

            entry = nextEntry;
        }

        return entry;
    }

    public boolean canMerge(MutablePaletteEntry other) {
        for (int i = 0; i < 6; i++) {
            var face1 = faces[i];
            var face2 = other.faces[i];

            if (face1 == null || face2 == null) continue;
            if (TextureData.fastEquals(face1, face2) == 0) continue;

            return false;
        }

        return true;
    }

    private void initTintDependencies() {
        if (tintDependencies != null) return;
        tintDependencies = new Int2IntOpenHashMap();
    }

    private void addTints(Int2IntMap tints) {
        initTintDependencies();

        this.tintDependencies.putAll(tints);
    }

    private void addTint(int tint) {
        initTintDependencies();

        this.tintDependencies.put(tint, 0);
    }


    public void addFaces(MutablePaletteEntry other) {
        for (int i = 0; i < 6; i++) {
            var face = other.faces[i];
            if (face == null) continue;

            faces[i] = face;
        }

        hasTransparent = hasTransparent || other.hasTransparent;
        other.mergedEntry = this;

        if (other.tintDependencies != null) {
            addTints(other.tintDependencies);
//            other.tintDependencies = null;
        }

        if (other.tint != tint) addTint(other.tint);
    }

    public boolean update(int normal, int tint, TextureData data) {
        this.tint = tint;
        faces[normal] = data;
        hasTransparent = hasTransparent || VoxelColor.a(data.color()) != 255;

        return true;
    }
}