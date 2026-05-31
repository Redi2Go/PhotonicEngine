package at.redi2go.photonics.core.rendering.world.registry.palete;

import at.redi2go.photonics.core.rendering.world.registry.object.WeakValue;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTexture;
import at.redi2go.photonics.core.rendering.world.registry.object.ObjectRegistry;

import java.util.concurrent.locks.ReadWriteLock;

public class PaletteRegistry extends ObjectRegistry<PaletteObject> {
    private final PaletteTexture paletteTexture;

    public PaletteRegistry(
            ReadWriteLock lock,
            PaletteTexture paletteTexture
    ) {
        super(lock);
        this.paletteTexture = paletteTexture;
    }
    
    public @WeakValue PaletteObject allocate(PaletteEntry entry) {
        if (entry instanceof MutablePaletteEntry me) me.makeWhole();
        entry.computeHashCode();
        
        return cacheObject(
                entry,
                e -> new PaletteObject(this, e),
                e -> e.allocate(paletteTexture)
        );
    }
}
