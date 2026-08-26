package at.redi2go.photonics.engine.iris.rendering;

import at.redi2go.photonics.engine.iris.pipeline.IrisPipeline;
import at.redi2go.photonics.engine.iris.properties.PhotonicsProperties;
import at.redi2go.photonics.engine.iris.properties.impl.PropertiesManager;
import at.redi2go.photonics.engine.iris.rendering.off.OffPipeline;
import at.redi2go.photonics.engine.iris.rendering.off.OffProperties;
import at.redi2go.photonics.engine.iris.rendering.restir.RestirPipeline;
import at.redi2go.photonics.engine.iris.rendering.restir.RestirProperties;
import at.redi2go.photonics.engine.iris.rendering.sharp.SharpPipeline;
import at.redi2go.photonics.engine.iris.rendering.sharp.SharpProperties;
import at.redi2go.photonics.engine.rendering.world.bakery.texture.AtlasDownloader;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public enum PhotonicsRenderer {
    OFF(OffProperties.class, OffPipeline::new),
    BASIC(SharpProperties.class, SharpPipeline::new),
    SHARP(SharpProperties.class, SharpPipeline::new),
    RESTIR(RestirProperties.class, RestirPipeline::new);

    private final String key = makeKey(name());
    private final Class<?> propertiesType;

    private final PhotonicsPipeline.Supplier<PhotonicsPipeline, Object> supplier;

    @SuppressWarnings("unchecked")
    <T extends PhotonicsPipeline, P> PhotonicsRenderer(Class<P> propertiesType, PhotonicsPipeline.Supplier<T, P> supplier) {
        this.propertiesType = propertiesType;
        this.supplier = (PhotonicsPipeline.Supplier<PhotonicsPipeline, Object>) supplier;
    }

    public String getKey() {
        return key;
    }

    public Class<?> getPropertiesType() {
        return propertiesType;
    }

    public static @Nullable PhotonicsPipeline createPipeline(
            PropertiesManager propertiesManager,
            Supplier<AtlasDownloader> atlasDownloaderSupplier,
            IrisPipeline irisPipeline
    ) {
        PhotonicsProperties properties  = propertiesManager.getProperties(PhotonicsProperties.class);
        if (!properties.isEnabled()) return null;

        PhotonicsRenderer renderer = properties.getRenderer();
        Object rendererProperties = propertiesManager.getProperties(renderer.propertiesType);

        return renderer.supplier.create(
                properties,
                rendererProperties,
                atlasDownloaderSupplier.get(),
                irisPipeline
        );
    }

    private static String makeKey(String name) {
        String[] parts = name.split("_");
        StringBuilder result = new StringBuilder();
        result.append(parts[0].toLowerCase());

        for (int i = 1; i < parts.length; i++) {
            var part = parts[i];
            if (part.isEmpty()) continue;

            result.append(part.substring(0, 1).toUpperCase());
            result.append(part.substring(1).toLowerCase());
        }

        return result.toString();
    }
}
