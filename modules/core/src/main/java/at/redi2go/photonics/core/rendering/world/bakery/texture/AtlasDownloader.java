package at.redi2go.photonics.core.rendering.world.bakery.texture;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import at.redi2go.photonics.api.mc.Id;

import java.util.function.Function;
import java.util.function.IntFunction;

public interface AtlasDownloader extends Disposable {
    void preloadTexture(Id atlasId);

    CpuTexture get(Id atlasId);
}
