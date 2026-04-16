package at.redi2go.photonics.core.rendering.world.block.palette;

import at.redi2go.photonics.core.rendering.world.block.VoxelColor;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.Nullable;

public final class MutablePaletteEntry extends PaletteEntry {
    int index = 0;

    public boolean canMerge(MutablePaletteEntry other) {
        return (presentFaces & other.presentFaces) == 0;
    }

    public void addFaces(MutablePaletteEntry other, @Nullable ObjectSet<MutablePaletteEntry>[] missingFaces) {
        for (int i = 0; i < 6; i++) {
            var face = other.faces[i];
            var missingFaceSet = missingFaces[i];

            // Should already be present as this face was already missing
            if (!hasFace(i)) missingFaceSet.remove(this);

            if (face == null) {
                if (missingFaceSet != null)
                    missingFaceSet.remove(other);

                continue;
            }

            faces[i] = face;
        }

        presentFaces = (short) (presentFaces | other.presentFaces);
        hasTransparent = hasTransparent || other.hasTransparent;

        usages+= other.usages;

        // Reinsert missing faces
        // These have to be removed to update the order
        for (int i = 0; i < 6; i++) {
            if (hasFace(i)) continue;

            // Should already be present as this face was already missing
            missingFaces[i].add(this);
        }
    }

    public boolean update(int normal, TextureData data) {
        TextureData face = faces[normal];
        if (face == null || data.gt(face)) {
            faces[normal] = data;
            hasTransparent = hasTransparent || VoxelColor.a(data.color()) != 255;

            return true;
        }

        return false;
    }
}