package at.redi2go.photonics.core.rendering.world.block.palette;

import at.redi2go.photonics.core.rendering.RenderingComponent;

public interface PaletteTexture extends RenderingComponent {
    PaletteTextureView reserveEntry();

    void upload();
}
