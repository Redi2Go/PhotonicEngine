package at.redi2go.photonics.core.iris.extensions;

import at.redi2go.photonics.api.shaders.PhotonicsProperties;
import at.redi2go.photonics.api.shaders.buffer.IBufferHolder;
import at.redi2go.photonics.api.shaders.uniform.IDynamicUniformHolder;
import at.redi2go.photonics.api.shaders.uniform.IUniformHolder;
import at.redi2go.photonics.core.iris.PhotonicsExtension;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;

public class TestingExtension implements PhotonicsExtension {
    private final PhotonicsProperties properties;
    private final AtlasDownloader atlasDownloader;

    public TestingExtension(PhotonicsProperties properties, AtlasDownloader atlasDownloader) {
        this.properties = properties;
        this.atlasDownloader = atlasDownloader;
    }

    @Override
    public void onFrameBegin() {

    }

    @Override
    public void registerUniforms(IUniformHolder uniforms) {

    }

    @Override
    public void registerDynamicUniforms(IDynamicUniformHolder dynamicUniforms) {

    }

    @Override
    public void registerBuffers(IBufferHolder buffers) {

    }

    @Override
    public void close() {
        atlasDownloader.close();
    }
}
