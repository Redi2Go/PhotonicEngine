package at.redi2go.photonics.core.rendering.world.registry.palete;

import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryMemory;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTexture;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTextureView;
import at.redi2go.photonics.core.rendering.world.registry.object.InnerWorldObject;
import at.redi2go.photonics.core.rendering.world.registry.object.ObjectRegistry;
import at.redi2go.photonics.core.rendering.world.registry.object.WorldObject;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeEntry;
import org.joml.Vector4i;

public class PaletteObject extends PaletteEntry implements WorldObject, VoxelTreeEntry {
    private final Reference ref;

    public PaletteObject(PaletteRegistry registry, PaletteEntry toCopy) {
        copyFrom(toCopy);

        this.ref = new Reference(registry);
    }

    @Override
    public int depth() {
        return -1;
    }

    @Override
    public void uploadTo(VoxelEntryMemory memory) {
        awaitAllocated();

        memory.setEntryFlag(hasTransparent);
        memory.setEntryData(ref.memoryOrThrow().entryData());
    }

    public void allocate(PaletteTexture texture) {
        var memory = ref.setMemory(texture::reserveEntry);

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
            faceData.z = face.normal();
            faceData.w = face.specular();

            memory.writeFace(i, faceData);
        }

        memory.upload();
    }


    @Override
    public boolean isAllocated() {
        return ref.isAllocated();
    }

    @Override
    public void awaitAllocated() {
        ref.awaitAllocated();
    }

    @Override
    public void acquireReference() {
        ref.acquireReference();
    }

    @Override
    public boolean tryAcquireReference() {
        return ref.tryAcquireReference();
    }

    @Override
    public void close() {
        ref.close();
    }

    private class Reference extends InnerWorldObject<PaletteTextureView> {
        public Reference(ObjectRegistry<?> registry) {
            super(registry);
        }

        @Override
        protected WorldObject getKey() {
            return PaletteObject.this;
        }
    }
}
