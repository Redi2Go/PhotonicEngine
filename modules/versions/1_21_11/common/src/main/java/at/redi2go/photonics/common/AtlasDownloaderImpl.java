package at.redi2go.photonics.common;

import at.redi2go.photonics.api.mc.Id;
import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.rendering.world.IgnoredInterruptedException;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasTexture;
import at.redi2go.photonics.core.rendering.world.bakery.texture.CpuTexture;
import at.redi2go.photonics.core.rendering.world.bakery.texture.Rgba8Texture;
import at.redi2go.photonics.core.rendering.world.block.TextureData;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import it.unimi.dsi.fastutil.Pair;
import net.irisshaders.iris.gl.texture.TextureAccess;
import net.irisshaders.iris.pbr.texture.PBRTextureHolder;
import net.irisshaders.iris.pbr.texture.PBRTextureManager;
import net.irisshaders.iris.targets.backed.NativeImageBackedSingleColorTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class AtlasDownloaderImpl implements AtlasDownloader, Runnable {
    //TODO Replace with ITextureFormat
    private final Map<TextureFormat, CpuTexture.Factory> textureFormats = new HashMap<>();
    private final ConcurrentHashMap<Id, CompletableFuture<AtlasTexture>> cache = new ConcurrentHashMap<>();

    private final TextureManager textureManager = Minecraft.getInstance().getTextureManager();

    public AtlasDownloaderImpl() {
        textureFormats.put(TextureFormat.RGBA8, Rgba8Texture::new);
        ResourceReloaderListener.add(this);
    }

    @Override
    public void run() {
        cache.clear();
    }

    private CompletableFuture<AtlasTexture> downloadTexture(Id atlasId) {
        return cache.computeIfAbsent(atlasId, (id) ->
                getTextureAtlas(id)
                        .thenCompose(this::getTextureData)
                        .thenCompose(this::getPbrTextures)
                        .thenCompose((result) -> {
                            var albedo = result.first();
                            var pbrHolder = result.second();

                            var normalTexture = getTextureData(pbrHolder.normalTexture());
                            var specularTexture = getTextureData(pbrHolder.specularTexture());

                            return CompletableFuture.allOf(normalTexture, specularTexture)
                                    .thenApply((ignored) -> new AtlasTexture(
                                            createCpuTexture(albedo, 0),
                                            createCpuTexture(normalTexture.getNow(null), TextureData.DEFAULT_NORMAL),
                                            createCpuTexture(specularTexture.getNow(null), TextureData.DEFAULT_SPECULAR)
                                    ));
                        })
        );
    }

    @Override
    public void preloadTexture(Id textureId) {
        downloadTexture(textureId);
    }

    @Override
    public AtlasTexture get(Id textureId) {
        try {
            return downloadTexture(textureId).get();
        } catch (InterruptedException e) {
            throw new IgnoredInterruptedException();
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void close() {
        ResourceReloaderListener.remove(this);
    }

    private record TextureDownload(AbstractTexture texture, int width, int height, int[] data) {
        public boolean isSameSize(TextureDownload other) {
            return other.width == width &&
                    other.height == height;
        }
    }

    private CompletableFuture<@Nullable TextureDownload> getTextureData(AbstractTexture texture) {
        if (texture instanceof NativeImageBackedSingleColorTexture)
            return CompletableFuture.completedFuture(null);

        var result = new CompletableFuture<TextureDownload>();

        try {
            GpuTexture gpuTexture = texture.getTexture();

            int width = gpuTexture.getWidth(0);
            int height = gpuTexture.getHeight(0);

            int byteSize = gpuTexture.getFormat().pixelSize() * width * height;

            var device = RenderSystem.getDevice();
            CommandEncoder commandEncoder = device.createCommandEncoder();

            Minecraft.getInstance().execute(() -> {
                GpuBuffer outputBuffer = device.createBuffer(
                        () -> "Photonics voxelization texture output",
                        GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_MAP_READ,
                        byteSize
                );

                commandEncoder.copyTextureToBuffer(gpuTexture, outputBuffer, 0, () -> {
                    try (var mappedView = commandEncoder.mapBuffer(outputBuffer, true, false); outputBuffer) {
                        IntBuffer buffer = mappedView.data().asIntBuffer();
                        int[] data = new int[byteSize >> 2];

                        buffer.rewind();
                        buffer.get(data, 0, buffer.remaining());

                        result.complete(new TextureDownload(texture, width, height, data));
                    } catch (Throwable t) {
                        result.completeExceptionally(t);
                    }
                }, 0);

            });
        } catch (Throwable t) {
            result.completeExceptionally(t);
        }

        return result;
    }

    private CompletableFuture<AbstractTexture> getTextureAtlas(Id atlasId) {
        var future = new CompletableFuture<AbstractTexture>();

        Minecraft.getInstance().execute(() -> {
            try {
                var texture = textureManager.getTexture((Identifier) (Object) atlasId);

                // Preload PBR texture, might be unnecessary
                var glTexture = (GlTexture) texture.getTexture();
                PBRTextureManager.INSTANCE.getOrLoadHolder(glTexture.glId());

                future.complete(texture);
            } catch (Throwable ex) {
                future.completeExceptionally(ex);
            }
        });

        return future;
    }

    private CompletableFuture<Pair<TextureDownload, PBRTextureHolder>> getPbrTextures(TextureDownload atlas) {
        var future = new CompletableFuture<Pair<TextureDownload, PBRTextureHolder>>();

        Minecraft.getInstance().execute(() -> {
            try {
                var glTexture = (GlTexture) atlas.texture.getTexture();
                var id = glTexture.glId();

                future.complete(Pair.of(atlas, PBRTextureManager.INSTANCE.getHolder(id)));
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    private @Nullable CpuTexture createCpuTexture(TextureDownload download, int defaultValue) {
        if (download == null) return null;

        CpuTexture.Factory textureFormat = textureFormats.get(download.texture.getTexture().getFormat());
        if (textureFormat == null) {
            Photonics.LOGGER.warn("Unsupported texture format: {}", download.texture.getTexture().getFormat());
            return null;
        }

        return textureFormat.create(download.width, download.height, defaultValue, download.data);
    }
}
