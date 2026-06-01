package at.redi2go.photonics.core.rendering.world.registry.palete;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryMemory;
import at.redi2go.photonics.core.rendering.world.block.TextureData;
import at.redi2go.photonics.core.rendering.world.block.VoxelColor;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteEntry;
import at.redi2go.photonics.core.rendering.world.registry.block.builder.VoxelData;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeEntry;
import org.jetbrains.annotations.Nullable;

public class MutablePaletteEntry extends PaletteEntry implements VoxelTreeEntry {
    public MutablePaletteEntry() {
        super();
    }

    public MutablePaletteEntry(PaletteEntry other) {
        super();
        copyFrom(other);
    }

    private void update(int normal, TextureData data) {
        faces[normal] = data;
        hasTransparent = hasTransparent || VoxelColor.a(data.color()) != 255;
    }

    public void update(VoxelTreeEntry entry) {
        if (entry instanceof VoxelData voxelData) {
            update(voxelData.normal, voxelData.textureData);
        } else if (entry instanceof MutablePaletteEntry paletteEntry) {
            for (int i = 0; i < 6; i++)
                update(i, paletteEntry.faces[i]);
        }
    }


    public static MutablePaletteEntry copyOf(@Nullable VoxelTreeEntry entry) {
        if (entry instanceof Disposable disposable) disposable.close();

        if (entry instanceof PaletteObject po) return new MutablePaletteEntry(po);
        if (entry instanceof MutablePaletteEntry mo) return mo;

        return new MutablePaletteEntry();
    }

    public void makeWhole() {
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

    @Override
    public int depth() {
        return -1;
    }

    @Override
    public void uploadTo(VoxelEntryMemory memory) {
        throw new UnsupportedOperationException("uploadTo");
    }
}
