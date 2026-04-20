package at.redi2go.photonics.core.rendering.world.allocator;

import at.redi2go.photonics.core.rendering.world.block.palette.PaletteEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTexture;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTextureView;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import org.joml.Vector4i;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class PaletteAllocation extends PaletteEntry implements HashedObject {
    private volatile int count = 0;
    protected final BufferWorldAllocator allocator;

    private PaletteTextureView memory;

    public PaletteAllocation(PaletteEntry toCopy, BufferWorldAllocator allocator) {
        this.allocator = allocator;

        copyFrom(toCopy);

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
    public int count() {
        return count;
    }

    @Override
    public boolean isAllocated() {
        return memory != null;
    }

    public void allocate(PaletteTexture paletteTexture) {
        this.memory = paletteTexture.reserveEntry();

        var faceData = new Vector4i();

        for (int i = 0; i < faces.length; i++) {
            var face = faces[i];
            if (face == null) {
                faceData.set(0);
                memory.writeFace(i, faceData);

                continue;
            }

            faceData.x = face.blockId();
            faceData.y = face.color();
            faceData.z = 0;
            faceData.w = 0;

            memory.writeFace(i, faceData);
        }

        memory.upload();
    }

    public int begin() {
        return memory.pos();
    }

    @Override
    public void acquire() {
        while (true) {
            var previous = count;

            if (HANDLE.compareAndSet(this, previous, previous + 1))
                break;
        }
    }

    @Override
    public void close() {
        while (true) {
            var previous = count;
            var newValue = count - 1;
            if (newValue < 0) return;

            if (HANDLE.compareAndSet(this, previous, newValue)) {
                if (newValue == 0)
                    allocator.freeHashedObject(this);
            }
        }
    }

    @Override
    public void free() {
        memory.close();
    }

    private static final VarHandle HANDLE;

    static {
        try {
            HANDLE = MethodHandles.lookup()
                    .findVarHandle(PaletteAllocation.class, "count", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
