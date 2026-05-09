package at.redi2go.photonics.core.rendering.world.block.palette;

import at.redi2go.photonics.core.rendering.RenderingComponent;

public interface PaletteTexture extends RenderingComponent {
    void upload();

    PaletteTextureView reserveEntry();
}
