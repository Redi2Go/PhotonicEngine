package at.redi2go.photonics.core.rendering;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.iris.pipeline.buffer.IBufferHolder;
import at.redi2go.photonics.core.iris.pipeline.texture.ISamplerHolder;
import at.redi2go.photonics.core.iris.pipeline.uniform.IDynamicUniformHolder;
import at.redi2go.photonics.core.iris.pipeline.uniform.IUniformHolder;

public interface RenderingComponent extends Disposable {
    default void onFrameBegin() {}

    default void onSectionAdded(int x, int y, int z) {}

    default void onSectionChanged(int x, int y, int z) {}

    default void registerUniforms(IUniformHolder uniforms) {}

    default void registerDynamicUniforms(IDynamicUniformHolder dynamicUniforms) {}

    default void registerBuffers(IBufferHolder buffers) {}

    default void registerCustomTextures(ISamplerHolder samplers) {}

    @Override
    default void close() {}
}
