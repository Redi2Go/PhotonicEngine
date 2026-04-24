package at.redi2go.photonics.common.mixins.iris.pipeline;

import at.redi2go.photonics.api.shaders.IShaderPack;
import at.redi2go.photonics.common.AtlasDownloaderImpl;
import at.redi2go.photonics.common.MinecraftBlockMesher;
import at.redi2go.photonics.common.iris.pipeline.PipelineManagerExt;
import at.redi2go.photonics.common.test.VoxelizationTestExtension;
import at.redi2go.photonics.core.iris.PhotonicsExtension;
import at.redi2go.photonics.core.rendering.world.bakery.BlockMesher;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.PipelineManager;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PipelineManager.class)
public abstract class PipelineManagerMixin implements PipelineManagerExt {
    @Unique
    private PhotonicsExtension photonics;

    @Inject(method = "preparePipeline", at = @At("HEAD"))
    private void preparePipeline(NamespacedId currentDimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        if (photonics != null) return;

        //TODO Add to more sensible spot
        BlockMesher.REGISTRY.addDefault(new MinecraftBlockMesher());

        var shaderPack = (IShaderPack) Iris.getCurrentPack().orElse(null);
        if (shaderPack == null) return;

        var properties = shaderPack.properties();
        photonics = new VoxelizationTestExtension(properties, new AtlasDownloaderImpl());
//        if (!properties.isPhotonicsEnabled()) {
//            photonics = new PhotonicsExtension.Disabled();
//        } else {
//            photonics = new VoxelizationTestExtension(properties, new AtlasDownloaderImpl());
//        }
//        photonics = PhotonicsExtension.create(
//                properties,
//                AtlasDownloaderImpl::new
//        );
    }

    @Override
    public Optional<PhotonicsExtension> photonics() {
        return Optional.ofNullable(photonics);
    }

    @Inject(method = "destroyPipeline", at = @At("HEAD"))
    private void destroyEverything(CallbackInfo ci) {
        if (photonics != null) {
            photonics.close();
            photonics = null;
        }
    }
}
