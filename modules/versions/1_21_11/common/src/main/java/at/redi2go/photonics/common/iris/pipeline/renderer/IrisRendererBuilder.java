package at.redi2go.photonics.common.iris.pipeline.renderer;

import at.redi2go.photonics.common.iris.pipeline.builder.PipelineActionBuilder;
import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class IrisRendererBuilder implements PipelineActionBuilder {
    private final String name;

    private final List<DeferredIrisRenderer.Pass> passes = new ArrayList<>();
    private final List<DeferredIrisRenderer> commonRenderers;

    private boolean finished = false;

    public IrisRendererBuilder(
            String name,
            List<DeferredIrisRenderer> commonRenderers
    ) {
        this.name = name;
        this.commonRenderers = commonRenderers;
    }

    @Override
    public boolean addDebugGroup(String name) {
        finished = true;
        return false;
    }

    @Override
    public boolean addDeferredPass(
            String name,
            @Nullable IrisFramebuffer framebuffer,
            @Nullable String fragmentShader,
            @Nullable String vertexShader
    ) {
        if (finished) return false;

        passes.add(
                new DeferredIrisRenderer.Pass(
                        name,
                        fragmentShader,
                        vertexShader == null ? "/photonics/rendering/screen.vsh" : vertexShader,
                        framebuffer
                )
        );

        return true;
    }

    public IrisRenderer buildAction() {
        if (passes.isEmpty()) return EmptyIrisRenderer.INSTANCE;

        var result = new DeferredIrisRenderer(name, passes);
        commonRenderers.add(result);

        return result;
    }
}
