package at.redi2go.photonics.common.iris.pipeline.renderer;

import at.redi2go.photonics.core.iris.pipeline.texture.ISamplerHolder;
import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import at.redi2go.photonics.core.iris.pipeline.rendering.IrisRenderer;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class IrisRendererImpl implements IrisRenderer {
    private final String name;

    private final List<Pass> passes;
    private PhotonicsRenderer activeRenderer;

    public IrisRendererImpl(
            String name,
            List<Pass> passes
    ) {
        this.name = name;
        this.passes = ImmutableList.copyOf(passes);
    }

    public String name() {
        return name;
    }

    @Override
    public void renderAll() {
        Objects.requireNonNull(activeRenderer).renderAll();
    }

    public List<Pass> getPasses() {
        return passes;
    }

    public void setActive(@Nullable PhotonicsRenderer activeImpl) {
        this.activeRenderer = activeImpl;
    }

    public record Pass(
            String name,
            @Nullable String fragmentShader,
            @Nullable String vertexShader,
            @Nullable IrisFramebuffer framebuffer
    ) {
    }
}
