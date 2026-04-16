package at.redi2go.photonics.core.iris;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.shaders.PhotonicsProperties;
import at.redi2go.photonics.api.shaders.buffer.IBufferHolder;
import at.redi2go.photonics.api.shaders.uniform.IDynamicUniformHolder;
import at.redi2go.photonics.api.shaders.uniform.IUniformHolder;
import at.redi2go.photonics.core.iris.extensions.TestingExtension;

/**
 * The extension for Iris (maybe aperture?), handles world building, light list, ect.
 */
public interface PhotonicsExtension extends Disposable {
    void registerUniforms(IUniformHolder uniforms);

    void registerDynamicUniforms(IDynamicUniformHolder dynamicUniforms);

    void registerBuffers(IBufferHolder buffers);

    static PhotonicsExtension create(PhotonicsProperties properties) {
        if (!properties.isPhotonicsEnabled()) return new Disabled();

        return new TestingExtension();
    }

    class Disabled implements PhotonicsExtension {
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