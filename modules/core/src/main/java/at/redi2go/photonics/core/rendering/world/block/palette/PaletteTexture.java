package at.redi2go.photonics.core.rendering.world.block.palette;

import at.redi2go.photonics.api.Disposable;

public interface PaletteTexture extends Disposable {
    PaletteTextureView reserveEntry();
}
