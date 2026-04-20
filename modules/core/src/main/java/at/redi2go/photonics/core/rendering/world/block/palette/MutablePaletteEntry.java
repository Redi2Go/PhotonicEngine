package at.redi2go.photonics.core.rendering.world.block.palette;

import at.redi2go.photonics.core.rendering.world.block.VoxelColor;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class MutablePaletteEntry extends PaletteEntry {
    int index = 0;
    List<MutablePaletteEntry> dependants;

    public MutablePaletteEntry() {
        super();
    }

    public MutablePaletteEntry(PaletteEntry other) {
        super();
        copyFrom(other);
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

    private void initDependants() {
        if (dependants != null) return;

        dependants = new ArrayList<>();
    }

    public void addFaces(MutablePaletteEntry other, Object2ObjectMap<MutablePaletteEntry, MutablePaletteEntry> interner) {
        for (int i = 0; i < 6; i++) {
            var face = other.faces[i];
            if (face == null) continue;

            faces[i] = face;
            presentFaces |= (short) (1 << i);
        }

        hasTransparent = hasTransparent || other.hasTransparent;
        usages+= other.usages;

        initDependants();
        if (other.dependants != null) {
            for (var dependant : other.dependants) {
                dependants.add(dependant);
                interner.put(dependant, this);
            }

            other.dependants = null;
        }

        dependants.add(other);
        interner.put(other, this);
    }

    public boolean update(int normal, TextureData data) {
        faces[normal] = data;
        hasTransparent = hasTransparent || VoxelColor.a(data.color()) != 255;


        return false;
    }
}