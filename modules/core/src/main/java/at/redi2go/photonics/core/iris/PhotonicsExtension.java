package at.redi2go.photonics.core.iris;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.shaders.PhotonicsProperties;
import at.redi2go.photonics.api.shaders.buffer.IBufferHolder;
import at.redi2go.photonics.api.shaders.uniform.IDynamicUniformHolder;
import at.redi2go.photonics.api.shaders.uniform.IUniformHolder;
import at.redi2go.photonics.core.iris.extensions.TestingExtension;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;

import java.util.function.Supplier;

/**
 * The extension for Iris (maybe aperture?), handles world building, light list, ect.
 */
public interface PhotonicsExtension extends Disposable {
    void onFrameBegin();

    void registerUniforms(IUniformHolder uniforms);

    void registerDynamicUniforms(IDynamicUniformHolder dynamicUniforms);

    void registerBuffers(IBufferHolder buffers);

    static PhotonicsExtension create(
            PhotonicsProperties properties,
            Supplier<AtlasDownloader> atlasDownloader
    ) {
        if (!properties.isPhotonicsEnabled()) return new Disabled();

        return new TestingExtension(
                properties,
                atlasDownloader.get()
        );
    }

    class Disabled implements PhotonicsExtension {
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

        }
    }
}