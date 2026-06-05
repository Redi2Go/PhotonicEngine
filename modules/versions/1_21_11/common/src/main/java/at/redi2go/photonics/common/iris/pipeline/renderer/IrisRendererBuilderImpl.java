package at.redi2go.photonics.common.iris.pipeline.renderer;

import at.redi2go.photonics.core.iris.pipeline.rendering.IrisRenderer;
import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class IrisRendererBuilderImpl implements IrisRenderer.Builder {
    private final String name;

    private final List<IrisRendererImpl.Pass> passes = new ArrayList<>();
    private final List<IrisRendererImpl> commonRenderers;

    public IrisRendererBuilderImpl(
            String name,
            List<IrisRendererImpl> commonRenderers
    ) {
        this.name = name;
        this.commonRenderers = commonRenderers;
    }

    @Override
    public IrisRenderer.Builder addPass(
            String name,
            @Nullable String fragmentShader,
            @Nullable String vertexShader,
            @Nullable IrisFramebuffer framebuffer
    ) {
        passes.add(
                new IrisRendererImpl.Pass(
                        name,
                        fragmentShader,
                        vertexShader == null ? "/photonics/rendering/screen.vsh" : vertexShader,
                        framebuffer
                )
        );

        return this;
    }

    @Override
    public IrisRenderer build() {
        if (passes.isEmpty()) return EmptyRenderer.INSTANCE;

        var result = new IrisRendererImpl(name, passes);
        commonRenderers.add(result);

        return result;
    }
}
