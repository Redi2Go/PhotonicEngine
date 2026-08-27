package at.redi2go.photonics.common.iris.pipeline.renderer;

import at.redi2go.photonics.common.iris.IrisUtil;
import at.redi2go.photonics.common.iris.pipeline.PipelineAction;
import at.redi2go.photonics.common.iris.pipeline.renderer.composite.PhCompositeRenderer;
import at.redi2go.photonics.engine.iris.pipeline.IrisRenderer;
import at.redi2go.photonics.engine.iris.pipeline.defines.IrisDynamicDefines;
import at.redi2go.photonics.engine.iris.pipeline.textures.IrisFramebuffer;
import at.redi2go.photonics.game.minecraft.Id;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class DeferredIrisPassAction implements IrisPassAction, PipelineAction {
    private final String name;

    private final List<Pass> passes;
    private PhCompositeRenderer activeRenderer;

    public DeferredIrisPassAction(
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

    public void setActive(@Nullable PhCompositeRenderer activeImpl) {
        this.activeRenderer = activeImpl;
    }

    public record Pass(
            String name,
            @Nullable String fragmentShader,
            @Nullable String vertexShader,
            @Nullable IrisFramebuffer framebuffer,
            BiConsumer<IrisRenderer.CompositePassBuilder, Id> builderAction,
            List<Runnable> actions
    ) {
        public boolean hasFragmentShader() {
            return fragmentShader != null;
        }

        public void applyBuilder(IrisDynamicDefines dynamicDefines) {
            builderAction.accept(new DeferredPassBuilderImpl(dynamicDefines), IrisUtil.getCurrentDimension());
        }
    }

    private record DeferredPassBuilderImpl(IrisDynamicDefines defines) implements IrisRenderer.CompositePassBuilder {
        @Override
        public void stringDefine(String name, String value) {
            defines.stringDefine(name, value);
        }

        @Override
        public void intDefine(String name, int value) {
            defines.intDefine(name, value);
        }

        @Override
        public void floatDefine(String name, float value) {
            defines.floatDefine(name, value);
        }

        @Override
        public void enumDefine(String name, Enum<?> value) {
            defines.enumDefine(name, value);
        }
    }
}
