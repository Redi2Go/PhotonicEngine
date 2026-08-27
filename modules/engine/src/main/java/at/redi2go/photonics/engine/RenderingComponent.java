package at.redi2go.photonics.engine;

import at.redi2go.photonics.game.Disposable;
import at.redi2go.photonics.engine.iris.pipeline.buffers.IrisBufferHolder;
import at.redi2go.photonics.engine.iris.pipeline.textures.IrisSamplerHolder;
import at.redi2go.photonics.engine.iris.pipeline.uniforms.IrisDynamicUniformHolder;
import at.redi2go.photonics.engine.iris.pipeline.uniforms.IrisUniformHolder;

public interface RenderingComponent extends Disposable {
    default void onFrameBegin() {}

    default void onSectionAdded(int x, int y, int z) {}

    default void onSectionChanged(int x, int y, int z) {}

    default void registerUniforms(IrisUniformHolder uniforms) {}

    default void registerDynamicUniforms(IrisDynamicUniformHolder dynamicUniforms) {}

    default void registerBuffers(IrisBufferHolder buffers) {}

    default void registerCustomTextures(IrisSamplerHolder samplers) {}

    @Override
    default void close() {}
}
