package at.redi2go.photonics.core.rendering.world.registry;

import at.redi2go.photonics.core.rendering.world.block.palette.PaletteEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTexture;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTextureView;
import at.redi2go.photonics.core.rendering.world.registry.objects.WorldObject;
import org.joml.Vector4i;

import java.util.List;

public class PaletteObject extends WorldObject<PaletteTextureView> {
    private final Entry entry;

    public PaletteObject(WorldRegistry worldRegistry, PaletteEntry toCopy) {
        super(worldRegistry);

        this.entry = new Entry(toCopy);
    }

    public PaletteEntry getEntry() {
        return entry;
    }

    public int entryData() {
        return memoryOrThrow().entryData();
    }

    public void allocate() {
        var memory = setMemory(() -> worldRegistry.paletteTexture().reserveEntry());

        entry.writeTo(memory);
        memory.upload();
    }

    private static class Entry extends PaletteEntry {
        private Entry(PaletteEntry toCopy) {
            copyFrom(toCopy);
        }

        public void writeTo(PaletteTextureView texture) {
            var faceData = new Vector4i();

            for (int i = 0; i < faces.length; i++) {
                var face = faces[i];
                if (face == null) {
                    faceData.set(0);
                    texture.writeFace(i, faceData);

                    continue;
                }

                faceData.x = face.blockId();
                faceData.y = face.color();
                faceData.z = 0;
                faceData.w = 0;

                texture.writeFace(i, faceData);
            }
        }
    }
}
