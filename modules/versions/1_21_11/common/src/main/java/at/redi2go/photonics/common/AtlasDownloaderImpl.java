package at.redi2go.photonics.common;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import at.redi2go.photonics.api.mc.Id;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;
import at.redi2go.photonics.core.rendering.world.bakery.texture.CpuTexture;
import at.redi2go.photonics.core.rendering.world.bakery.texture.Rgba8Texture;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Unit;
import org.jspecify.annotations.NonNull;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class AtlasDownloaderImpl implements AtlasDownloader, Runnable {
    //TODO Replace with ITextureFormat
    private final Map<TextureFormat, CpuTexture.Factory> textureFormats = new HashMap<>();
    private final ConcurrentHashMap<Id, CompletableFuture<CpuTexture>> cache = new ConcurrentHashMap<>();

    private final TextureManager textureManager = Minecraft.getInstance().getTextureManager();

    public AtlasDownloaderImpl() {
        textureFormats.put(TextureFormat.RGBA8, Rgba8Texture::new);
        ResourceReloaderListener.add(this);
    }

    @Override
    public void run() {
        cache.clear();
    }

    private CompletableFuture<CpuTexture> downloadTexture(Id atlasId) {
        return cache.computeIfAbsent(atlasId, (id) -> {
            var future = new CompletableFuture<CpuTexture>();

            try {
                var texture = textureManager.getTexture((Identifier) (Object) atlasId).getTexture();

                CpuTexture.Factory textureFormat = textureFormats.get(texture.getFormat());
                if (textureFormat == null)
                    throw new IllegalArgumentException("Unsupported texture format: " + texture.getFormat());

                int width = texture.getWidth(0);
                int height = texture.getHeight(0);

                int byteSize = texture.getFormat().pixelSize() * width * height;

                var device = RenderSystem.getDevice();

                Minecraft.getInstance().execute(() -> {
                    GpuBuffer outputBuffer = device.createBuffer(() -> "Photonics voxelization texture output", GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_MAP_READ, byteSize);
                    CommandEncoder commandEncoder = device.createCommandEncoder();

                    commandEncoder.copyTextureToBuffer(texture, outputBuffer, 0, () -> {
                        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(outputBuffer, true, false)) {
                            IntBuffer buffer = mappedView.data().asIntBuffer();
                            int[] data = new int[byteSize >> 2];

                            buffer.rewind();
                            buffer.get(data, 0, buffer.remaining());

                            future.complete(textureFormat.create(width, height, data));
                        } catch (Throwable e) {
                            future.completeExceptionally(e);
                        } finally {
                            outputBuffer.close();
                        }
                    }, 0);
                });
            } catch (Throwable e) {
                future.completeExceptionally(e);
            }

            return future;
        });
    }

    @Override
    public void preloadTexture(Id textureId) {
        downloadTexture(textureId);
    }

    @Override
    public CpuTexture get(Id textureId) {
        try {
            return downloadTexture(textureId).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void close() {
        ResourceReloaderListener.remove(this);
    }
}
