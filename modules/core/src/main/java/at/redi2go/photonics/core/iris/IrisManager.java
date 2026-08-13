package at.redi2go.photonics.core.iris;

import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.iris.patching.ShaderPatcher;
import at.redi2go.photonics.core.iris.pipeline.DefineHolder;
import at.redi2go.photonics.core.iris.pipeline.IrisPipeline;
import at.redi2go.photonics.core.iris.pipeline.buffer.IBufferHolder;
import at.redi2go.photonics.core.iris.pipeline.texture.ISamplerHolder;
import at.redi2go.photonics.core.iris.pipeline.uniform.IDynamicUniformHolder;
import at.redi2go.photonics.core.iris.pipeline.uniform.IUniformHolder;
import at.redi2go.photonics.core.iris.properties.PhotonicsProperties;
import at.redi2go.photonics.core.iris.properties.impl.PropertiesManager;
import at.redi2go.photonics.core.iris.rendering.PhotonicsPipeline;
import at.redi2go.photonics.core.iris.rendering.PhotonicsRenderer;
import at.redi2go.photonics.core.rendering.lights.HandheldItemSupplier;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

public class IrisManager {
    public static final IrisManager INSTANCE = new IrisManager();

    private static final PropertiesManager propertiesManager = new PropertiesManager();

    private static @Nullable ShaderPatcher activePatcher = null;
    private static @Nullable PhotonicsProperties activeProperties = null;
    private static @Nullable PhotonicsPipeline activePipeline = null;

    public static Optional<ShaderPatcher> getShaderPatcher() {
        return  Optional.ofNullable(activePatcher);
    }

    public static ShaderPatcher getShaderPatcherOrThrow() {
        return getShaderPatcher().orElseThrow();
    }

    public static Optional<PhotonicsProperties> getProperties() {
        return Optional.ofNullable(activeProperties);
    }

    public static PhotonicsProperties getPropertiesOrThrow() {
        return getProperties().orElseThrow();
    }

    public static boolean hasPipeline() {
        return activePipeline != null;
    }

    public static void setupShaderPatcher(@NonNls IrisPack pack, boolean patchEnabled) {
        Objects.requireNonNull(pack, "pack");

        activePatcher = new ShaderPatcher(pack);
        propertiesManager.setForceEnabled(!pack.ph$supportsPhotonics() && activePatcher.hasPatch() && patchEnabled);
    }

    public static void setupProperties(@NonNls Properties properties, @NonNls Logger logger) {
        Objects.requireNonNull(properties, "properties");
        Objects.requireNonNull(logger, "logger");

        destroyEverything(false);
        propertiesManager.setProperties(properties, logger);

        activeProperties = propertiesManager.getProperties(PhotonicsProperties.class);
    }
    
    public static void setupPipeline(
            @NonNls Supplier<AtlasDownloader> atlasDownloaderSupplier,
            @NonNls Supplier<HandheldItemSupplier> handheldItemSupplierSupplier,
            @NonNls IrisPipeline irisPipeline
    ) {
        Objects.requireNonNull(atlasDownloaderSupplier, "atlasDownloaderSupplier");
        Objects.requireNonNull(handheldItemSupplierSupplier, "handheldItemSupplierSupplier");
        Objects.requireNonNull(irisPipeline, "irisPipeline");

        if (activeProperties == null) throw new IllegalStateException("The renderer has not been set up");
        if (activePipeline != null) throw new IllegalStateException("Pipeline has already been created");
        
        activePipeline = PhotonicsRenderer.createPipeline(
                propertiesManager,
                atlasDownloaderSupplier,
                handheldItemSupplierSupplier,
                irisPipeline
        );
    }

    public static void onRender() {
        var pipeline = activePipeline;
        if (pipeline != null)
            pipeline.onRender();
    }

    public static void onFrameBegin() {
        var pipeline = activePipeline;
        if (pipeline != null)
            pipeline.onFrameBegin();
    }

    public static void onSectionAdded(int x, int y, int z) {
        var pipeline = activePipeline;
        if (pipeline != null)
            pipeline.onSectionAdded(x, y, z);
    }

    public static void onSectionChanged(int x, int y, int z) {
        var pipeline = activePipeline;
        if (pipeline != null)
            pipeline.onSectionChanged(x, y, z);
    }

    public static void registerVersionDefines(DefineHolder defines) {
        defines.stringDefine("PHOTONICS", "");
        defines.stringDefine("PHOTONICS_VERSION", Photonics.getVersionString());
    }

    public static void registerDefines(DefineHolder defines) {
        propertiesManager.registerDefines(defines);
    }

    public static void registerUniforms(IUniformHolder uniforms) {
        var pipeline = activePipeline;
        if (pipeline != null)
            pipeline.registerUniforms(uniforms);
    }

    public static void registerDynamicUniforms(IDynamicUniformHolder dynamicUniforms) {
        var pipeline = activePipeline;
        if (pipeline != null)
            pipeline.registerDynamicUniforms(dynamicUniforms);
    }

    public static void registerBuffers(IBufferHolder buffers) {
        var pipeline = activePipeline;
        if (pipeline != null)
            pipeline.registerBuffers(buffers);
    }

    public static void registerCustomTextures(ISamplerHolder samplers) {
        var pipeline = activePipeline;
        if (pipeline != null)
            pipeline.registerCustomTextures(samplers);
    }

    public static void destroyEverything() {
        destroyEverything(true);
    }

    private static void destroyEverything(boolean destroyPatcher) {
        var pipeline = activePipeline;
        if (pipeline == null) return;

        activePatcher = destroyPatcher ? null : activePatcher;
        activePipeline = null;
        pipeline.close();
    }

    private IrisManager() {
    }
}
