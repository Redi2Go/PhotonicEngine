package at.redi2go.photonics.core.rendering.world.registry;

import at.redi2go.photonics.core.rendering.world.block.palette.PaletteEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTexture;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTextureView;
import org.joml.Vector4i;

import java.util.List;

public class PaletteObject extends MemoryOwner<PaletteObject.Entry, PaletteTextureView> {
    private final Entry entry;

    public PaletteObject( WorldRegistry registry, PaletteEntry toCopy) {
        super(registry);

        this.entry = new Entry(toCopy);
    }

    @Override
    protected void loadDependants(List<ManagedRef<?>> output) {

    }

    @Override
    protected Entry getWrappedValue() {
        return entry;
    }

    public void allocate(PaletteTexture paletteTexture) {
        setMemory(paletteTexture.reserveEntry());
        entry.upload();
    }

    public class Entry extends PaletteEntry {
        private Entry(PaletteEntry toCopy) {
            copyFrom(toCopy);
        }

        public void awaitAllocated() {
            PaletteObject.this.awaitAllocated();
        }

        public int entryData() {
            return memoryOrThrow().entryData();
        }

        private void upload() {
            var texture = memoryOrThrow();
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

            texture.upload();
        }
    }
}
