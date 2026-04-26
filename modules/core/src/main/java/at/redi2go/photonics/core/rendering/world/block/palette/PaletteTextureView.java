package at.redi2go.photonics.core.rendering.world.block.palette;

import at.redi2go.photonics.api.Disposable;
import org.joml.Vector4i;

public interface PaletteTextureView extends Disposable {
    int pos();

    void writeFace(int face, Vector4i value);

    void upload();
}
