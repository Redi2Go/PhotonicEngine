package at.redi2go.photonics.core.iris;

import at.redi2go.photonics.api.shaders.PhotonicsProperties;
import at.redi2go.photonics.core.iris.extensions.BasicPipeline;
import at.redi2go.photonics.core.iris.extensions.OffPipeline;
import at.redi2go.photonics.core.iris.extensions.RestirPipeline;
import at.redi2go.photonics.core.iris.pipeline.buffer.IBufferHolder;
import at.redi2go.photonics.core.iris.pipeline.texture.ISamplerHolder;
import at.redi2go.photonics.core.iris.pipeline.uniform.IDynamicUniformHolder;
import at.redi2go.photonics.core.iris.pipeline.uniform.IUniformHolder;
import at.redi2go.photonics.core.iris.pipeline.rendering.IrisPipelineFactory;
import at.redi2go.photonics.core.rendering.RenderingComponent;
import at.redi2go.photonics.core.rendering.lights.HandheldItemSupplier;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;

import java.util.function.Supplier;

/**
 * The extension for Iris handles world building, light list, ect.
 */
public interface PhotonicsExtension extends RenderingComponent {
    void onRender();

    static PhotonicsExtension create(
            PhotonicsProperties properties,
            Supplier<AtlasDownloader> atlasDownloader,
            Supplier<HandheldItemSupplier> handheldItemSupplierSupplier,
            IrisPipelineFactory passFactory
    ) {
        if (!properties.isPhotonicsEnabled()) return new Disabled();

        return switch (properties.getLightingMode()) {
            case OFF -> new OffPipeline(properties, atlasDownloader.get(), handheldItemSupplierSupplier.get());
            case BASIC -> new BasicPipeline(properties, atlasDownloader.get(), handheldItemSupplierSupplier.get(), passFactory);
            case RESTIR -> new RestirPipeline(properties, atlasDownloader.get(), handheldItemSupplierSupplier.get(), passFactory);
        };
    }

    class Disabled implements PhotonicsExtension {
        @Override
        public void onFrameBegin() {

        }

        @Override
        public void onRender() {

        }

        @Override
        public void onSectionAdded(int x, int y, int z) {

        }

        @Override
        public void onSectionChanged(int x, int y, int z) {

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
        public void registerCustomTextures(ISamplerHolder samplers) {

        }


        @Override
        public void close() {

        }
    }
}
