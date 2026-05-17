package at.redi2go.photonics.core.config;

import at.redi2go.photonics.api.ModLoader;
import at.redi2go.photonics.api.mc.world.level.IBlock;
import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.config.adapter.BlockAdapter;
import at.redi2go.photonics.core.config.lights.LightRegistry;
import at.redi2go.photonics.core.config.lights.LightsProvider;
import at.redi2go.photonics.core.config.lights.block.LightBlock;
import at.redi2go.photonics.core.config.lights.color.LightColor;
import at.redi2go.photonics.core.config.lights.falloff.LightFalloff;
import at.redi2go.photonics.core.config.lights.intensity.LightIntensity;
import at.redi2go.photonics.core.config.lights.radius.LightRadius;
import at.redi2go.photonics.core.util.Lazy;
import com.google.common.collect.Sets;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class PhConfig {
    private static final String DEFAULT_CONFIG_RESOURCE = "assets/photonics/default_config.json";
    public static final Lazy<Path> PATH = Lazy.of(() -> ModLoader.getConfigDir().resolve("photonics.json"));

    static int mod = 0;

    private static PhStorage INSTANCE = new PhStorage();
    private static LightRegistry lightRegistry = new LightRegistry();
    private static final Set<PhConfigWatcher<?>> WATCHERS = new LinkedHashSet<>();

    private static final Set<LightsProvider> LIGHTS_PROVIDERS = Sets.newHashSet(
            new LightsProvider() {
                @Override
                public void registerLights(LightRegistry lights) {
                    for (final var lightGroup : INSTANCE.lights.values())
                        lightGroup.recordLights(
                                PhConfigOwner.INSTANCE,
                                lightRegistry,
                                INSTANCE.raytracedLights,
                                0
                        );
                }

                @Override
                public void registerChangeListener(Runnable consumer) {
                    throw new UnsupportedOperationException("registerChangeListener");
                }

                @Override
                public void clearListeners() {
                    throw new UnsupportedOperationException("clearListeners");
                }
            });

    static <T> Optional<T> getDefine(Variable.Type<T> type, String name) {
        return INSTANCE.defines.getValue(type, name);
    }

    public synchronized static <T> PhConfigWatcher<T> watch(Function<PhStorage, T> supplier, Consumer<T> consumer) {
        final PhConfigWatcher<T> PhConfigWatcher = new PhConfigWatcher<>(supplier, consumer);
        WATCHERS.add(PhConfigWatcher);

        return PhConfigWatcher;
    }

    synchronized static void removeWatcher(PhConfigWatcher<?> PhConfigWatcher) {
        WATCHERS.remove(PhConfigWatcher);
    }

    public synchronized static void prepareModify() {
        try {
            // Needed for PhConfigWatchers to detect a change
            INSTANCE = GSON.fromJson(GSON.toJson(INSTANCE), PhStorage.class);
        } catch (Exception e) {
            Photonics.LOGGER.error("Could not copy config", e);
        }
    }

    public synchronized static void reloadConfig() {
        mod++;
        if (Files.notExists(PATH.get())) {
            try {
                // This is safe from the watch thread
                // as the watch service is only created after reloadConfig returns
                copyDefaultConfig();
            } catch (Exception e) {
                Photonics.LOGGER.error("Could not create default config", e);

                throw e;
            }
        }

        Photonics.LOGGER.info("Reloading config");

        try (var reader = Files.newBufferedReader(PATH.get())) {
            INSTANCE = GSON.fromJson(reader, PhStorage.class);

            onChanged();
        } catch (Exception e) {
            Photonics.LOGGER.error("Could not reload config", e);
        }
    }

    public synchronized static void save() {
        mod++;
        PhConfigWatchThread.beginSave();

        try (var writer = Files.newBufferedWriter(PATH.get())) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            PhConfigWatchThread.reset();
            throw new RuntimeException(e);
        }

        PhConfigWatchThread.endSave();
    }

    private synchronized static void copyDefaultConfig() {
        try (var writer = Files.newOutputStream(PATH.get(), StandardOpenOption.CREATE_NEW)) {
            try (var input = Objects.requireNonNull(
                    Photonics.class
                            .getClassLoader()
                            .getResourceAsStream(DEFAULT_CONFIG_RESOURCE))) {

                input.transferTo(writer);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized static void onChangedSafe() {
        try {
            onChanged();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public synchronized static void onChanged() {
        final PhStorage config = INSTANCE;

        config.defines.setOwner(PhConfigOwner.INSTANCE);

        // create a new light list instance so PhConfigWatchers detect a change
        lightRegistry = new LightRegistry();
        for (var provider : LIGHTS_PROVIDERS)
            try {
                provider.registerLights(lightRegistry);
            } catch (Exception e) {
                Photonics.LOGGER.warn("Could not register lights provider", e);
            }

        lightRegistry.sort();

        for (var PhConfigWatcher : WATCHERS)
            PhConfigWatcher.reload(config);
    }

    public static synchronized void registerLightProvider(LightsProvider provider) {
        LIGHTS_PROVIDERS.add(provider);
        provider.registerChangeListener(PhConfig::onChangedSafe);

        onChanged();
    }

    public static synchronized void removeLightProvider(LightsProvider provider) {
        if (LIGHTS_PROVIDERS.remove(provider)) {
            provider.clearListeners();
            onChanged();
        }
    }

    public static boolean isMultiThreadingEnabled() {
        return INSTANCE.multiThreadingEnabled;
    }

    public static void setMultiThreadingEnabled(boolean enabled) {
        INSTANCE.multiThreadingEnabled = enabled;
    }

    public static LightRegistry getLightRegistry() {
        return lightRegistry;
    }

    public static boolean isVoxelizedBlocksEnabled() {
        return INSTANCE.enableVoxelizedBlocks;
    }

    public static void setVoxelizedBlocksEnabled(boolean enabled) {
        INSTANCE.enableVoxelizedBlocks = enabled;
    }

    public static Set<IBlock> getVoxelizedBlocks() {
        return INSTANCE.voxelizedBlocks;
    }

    public static boolean isVoxelized(IBlock block) {
        return getVoxelizedBlocks().contains(block);
    }

    public static void setVoxelized(IBlock block, boolean enabled) {
        if (enabled) {
            getVoxelizedBlocks().add(block);
        } else {
            getVoxelizedBlocks().remove(block);
        }
    }

    public static Map<IBlock, Boolean> getTracedOverrides() {
        return INSTANCE.raytracedLights;
    }


    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LightColor.class, new LightColor.Adapter())
            .registerTypeAdapter(LightIntensity.class, new LightIntensity.Adapter())
            .registerTypeAdapter(LightRadius.class, new LightRadius.Adapter())
            .registerTypeAdapter(LightFalloff.class, new LightFalloff.Adapter())
            .registerTypeAdapter(LightBlock.class, new LightBlock.Adapter())
            .registerTypeAdapter(IBlock.class, new BlockAdapter())
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .excludeFieldsWithModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .enableComplexMapKeySerialization()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .setStrictness(Strictness.LENIENT)
            .create();

    private PhConfig() {
        throw new IllegalStateException();
    }
}
