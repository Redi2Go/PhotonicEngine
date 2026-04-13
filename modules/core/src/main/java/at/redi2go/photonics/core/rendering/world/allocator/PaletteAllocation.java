package at.redi2go.photonics.core.rendering.world.allocator;

import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTexture;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTextureView;
import org.joml.Vector4f;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class PaletteAllocation extends PaletteEntry implements RefCounter {
    private volatile int count = 0;
    protected final BufferWorldAllocator allocator;

    private PaletteTextureView memory;

    public PaletteAllocation(PaletteEntry toCopy, BufferWorldAllocator allocator) {
        this.allocator = allocator;

        copyFrom(toCopy);
    }

    boolean isReady() {
        return memory != null;
    }

    void allocate(PaletteTexture paletteTexture) {
        this.memory = paletteTexture.reserveEntry();

        var faceData = new Vector4f();

        for (int i = 0; i < faces.length; i++) {
            var face = faces[i];
            if (face == null) {
                faceData.set(0);
                memory.writeFace(i, faceData);

                continue;
            }

            faceData.x = Float.intBitsToFloat(face.blockId());
            faceData.y = Float.intBitsToFloat(face.color());
            faceData.z = 0f;
            faceData.w = 0f;

            memory.writeFace(i, faceData);
        }

        memory.upload();
    }

    public int begin() {
        return memory.pos();
    }

    @Override
    public int count() {
        return count;
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
    public void release() {
        while (true) {
            var previous = count;
            var newValue = count - 1;
            if (newValue < 0) return;

            if (HANDLE.compareAndSet(this, previous, newValue)) {
                if (newValue == 0)
                    allocator.enqueueFreedObject(this);

            }
        }
    }

    @Override
    public void close() {
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
