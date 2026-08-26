package at.redi2go.photonics.common.iris.pipeline.renderer.composite;

import at.redi2go.photonics.common.iris.pipeline.renderer.DeferredIrisPassAction;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.gl.buffer.ShaderStorageBufferHolder;
import net.irisshaders.iris.gl.image.GlImage;
import net.irisshaders.iris.gl.texture.TextureAccess;
import net.irisshaders.iris.pathways.CenterDepthSampler;
import net.irisshaders.iris.pipeline.CompositePass;
import net.irisshaders.iris.pipeline.CompositeRenderer;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.programs.ComputeSource;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import net.irisshaders.iris.targets.BufferFlipper;
import net.irisshaders.iris.targets.RenderTargets;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class PhCompositeRenderer extends CompositeRenderer {
    private final String name;

    public PhCompositeRenderer(
            String name,
            WorldRenderingPipeline pipeline,
            PackDirectives packDirectives,
            ProgramSource[] sources,
            ComputeSource[][] computes,
            RenderTargets renderTargets,
            ShaderStorageBufferHolder holder,
            TextureAccess noiseTexture,
            FrameUpdateNotifier updateNotifier,
            CenterDepthSampler centerDepthSampler,
            BufferFlipper bufferFlipper,
            Supplier<ShadowRenderTargets> shadowTargetsSupplier,
            Object2ObjectMap<String, TextureAccess> customTextureIds,
            Object2ObjectMap<String, TextureAccess> irisCustomTextures,
            Set<GlImage> customImages,
            CustomUniforms customUniforms,
            List<DeferredIrisPassAction.Pass> passes
    ) {
        super(
                pipeline,
                CompositePass.DEFERRED,
                packDirectives,
                sources,
                computes,
                renderTargets,
                holder,
                noiseTexture,
                updateNotifier,
                centerDepthSampler,
                bufferFlipper,
                shadowTargetsSupplier,
                TextureStage.DEFERRED,
                customTextureIds,
                irisCustomTextures,
                customImages,
                ImmutableMap.of(),
                customUniforms
        );

        this.name = name;
        for (CompositeRendererPassExt pass : getPasses()) {
            var definition = passes.get(pass.getIndex());

            pass.setDebugName(definition.name());
            pass.setActions(definition.actions());
            pass.setFramebuffer(definition.framebuffer());
        }
    }

    public String getName() {
        return name;
    }

    private List<CompositeRendererPassExt> getPasses() {
        return ((CompositeRendererAccessor) this).getPasses();
    }

    @Override
    public void recalculateSizes() {
        for (CompositeRendererPassExt pass : getPasses())
            pass.updateSize();
    }
}
