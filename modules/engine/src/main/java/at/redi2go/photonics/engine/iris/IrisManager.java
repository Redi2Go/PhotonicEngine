package at.redi2go.photonics.engine.iris;

import at.redi2go.photonics.engine.Photonics;
import at.redi2go.photonics.engine.iris.patching.ShaderPatcher;
import at.redi2go.photonics.engine.iris.pipeline.defines.IrisDefineHolder;
import at.redi2go.photonics.engine.iris.pipeline.IrisPipeline;
import at.redi2go.photonics.engine.iris.pipeline.buffers.IrisBufferHolder;
import at.redi2go.photonics.engine.iris.pipeline.defines.IrisDynamicDefines;
import at.redi2go.photonics.engine.iris.pipeline.textures.IrisSamplerHolder;
import at.redi2go.photonics.engine.iris.pipeline.uniforms.IrisDynamicUniformHolder;
import at.redi2go.photonics.engine.iris.pipeline.uniforms.IrisUniformHolder;
import at.redi2go.photonics.engine.iris.properties.PhotonicsProperties;
import at.redi2go.photonics.engine.iris.properties.impl.PropertiesManager;
import at.redi2go.photonics.engine.iris.rendering.PhotonicsPipeline;
import at.redi2go.photonics.engine.iris.rendering.PhotonicsRenderer;
import at.redi2go.photonics.engine.rendering.world.bakery.texture.AtlasDownloader;
import at.redi2go.photonics.game.minecraft.Id;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class IrisManager {
    private static final PropertiesManager propertiesManager = new PropertiesManager();

    private static @Nullable ShaderPatcher activePatcher = null;
    private static @Nullable PhotonicsProperties activeProperties = null;
    private static @Nullable PhotonicsPipeline activePipeline = null;
    private static Set<DefineGroup> dynamicDefines = new HashSet<>();

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

    public static IrisDynamicDefines newDynamicDefines() {
        return new DefineGroup(new ArrayList<>());
    }

    public static void prepareShaderPatcher(@NonNull IrisPack pack, boolean patchEnabled) {
        Objects.requireNonNull(pack, "pack");

        activePatcher = new ShaderPatcher(pack);
        propertiesManager.setForceEnabled(!pack.ph$supportsPhotonics() && activePatcher.hasPatch() && patchEnabled);
    }

    public static void prepareProperties(@NonNull Properties properties, @NonNull Logger logger) {
        Objects.requireNonNull(properties, "properties");
        Objects.requireNonNull(logger, "logger");

        destroyEverything(false);
        propertiesManager.setProperties(properties, logger);

        activeProperties = propertiesManager.getProperties(PhotonicsProperties.class);
    }
    
    public static void preparePipeline(
            @NonNull Supplier<AtlasDownloader> atlasDownloaderSupplier,
            @NonNull IrisPipeline irisPipeline
    ) {
        Objects.requireNonNull(atlasDownloaderSupplier, "atlasDownloaderSupplier");
        Objects.requireNonNull(irisPipeline, "irisPipeline");

        if (activeProperties == null) throw new IllegalStateException("The renderer has not been set up");
        if (activePipeline != null) throw new IllegalStateException("Pipeline has already been created");
        
        activePipeline = PhotonicsRenderer.createPipeline(
                propertiesManager,
                atlasDownloaderSupplier,
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

    public static void registerVersionDefines(IrisDefineHolder defines) {
        defines.stringDefine("PHOTONICS", "");
        defines.stringDefine("PHOTONICS_VERSION", Photonics.getVersionString());
    }

    public static void registerStaticDefines(IrisDefineHolder defines) {
        propertiesManager.registerDefines(defines);
    }

    public static void registerDynamicDefines(IrisDefineHolder defines, Id dimension) {
        for (var group : dynamicDefines) {
            for (var define : group.defines())
                define.accept(defines, dimension);
        }
    }

    public static void registerUniforms(IrisUniformHolder uniforms) {
        var pipeline = activePipeline;
        if (pipeline != null)
            pipeline.registerUniforms(uniforms);
    }

    public static void registerDynamicUniforms(IrisDynamicUniformHolder dynamicUniforms) {
        var pipeline = activePipeline;
        if (pipeline != null)
            pipeline.registerDynamicUniforms(dynamicUniforms);
    }

    public static void registerBuffers(IrisBufferHolder buffers) {
        var pipeline = activePipeline;
        if (pipeline != null)
            pipeline.registerBuffers(buffers);
    }

    public static void registerCustomTextures(IrisSamplerHolder samplers) {
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

        dynamicDefines.clear();
    }

    private record DefineGroup(List<BiConsumer<IrisDefineHolder, Id>> defines) implements IrisDynamicDefines {
        DefineGroup {
            dynamicDefines.add(this);
        }

        @Override
        public IrisDynamicDefines withDefine(BiConsumer<IrisDefineHolder, Id> consumer) {
            defines.add(consumer);

            return this;
        }

        @Override
        public IrisDynamicDefines withDefines(List<BiConsumer<IrisDefineHolder, Id>> consumers) {
            defines.addAll(consumers);

            return this;
        }

        @Override
        public void close() {
            dynamicDefines.remove(this);
        }
    }

    private IrisManager() {
    }
}
