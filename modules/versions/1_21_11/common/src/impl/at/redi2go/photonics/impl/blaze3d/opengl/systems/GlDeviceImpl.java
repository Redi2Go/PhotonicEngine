package at.redi2go.photonics.impl.blaze3d.opengl.systems;

import at.redi2go.photonics.game.blaze3d.buffers.BufferUsage;
import at.redi2go.photonics.game.blaze3d.buffers.IGpuBuffer;
import at.redi2go.photonics.game.blaze3d.systems.ICommandEncoder;
import at.redi2go.photonics.game.blaze3d.systems.IGpuDevice;
import at.redi2go.photonics.game.blaze3d.textures.IAddressMode;
import at.redi2go.photonics.game.blaze3d.textures.IFilterMode;
import at.redi2go.photonics.game.blaze3d.textures.IGpuSampler;
import at.redi2go.photonics.game.blaze3d.textures.IGpuTexture;
import at.redi2go.photonics.game.blaze3d.textures.TextureFormat;
import at.redi2go.photonics.game.blaze3d.textures.TextureUsage;
import at.redi2go.photonics.impl.blaze3d.opengl.GlDebugLabelsExt;
import at.redi2go.photonics.impl.blaze3d.opengl.textures.GlTexture;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlDebugLabel;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import org.joml.Vector3ic;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL12;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.function.Supplier;

@Mixin(GlDevice.class)
public abstract class GlDeviceImpl implements IGpuDevice {
    @Shadow
    public abstract CommandEncoder createCommandEncoder();

    @Shadow
    public abstract GpuSampler createSampler(AddressMode addressMode, AddressMode addressMode2, FilterMode filterMode, FilterMode filterMode2, int i, OptionalDouble optionalDouble);

    @Shadow
    public abstract GpuBuffer createBuffer(@Nullable Supplier<String> supplier, int i, long l);

    @Shadow
    public abstract GpuBuffer createBuffer(@Nullable Supplier<String> supplier, int i, ByteBuffer byteBuffer);

    @Shadow @Final private GlDebugLabel debugLabels;

    @Override
    public ICommandEncoder ph$createCommandEncoder() {
        return (ICommandEncoder) createCommandEncoder();
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public IGpuSampler ph$createSampler(IAddressMode addressModeU, IAddressMode addressModeV, IFilterMode minFilter, IFilterMode magFilter, int maxAnisotropy, OptionalDouble maxLod) {
        return (IGpuSampler) createSampler(
                (AddressMode) (Object) addressModeU,
                (AddressMode) (Object) addressModeV,
                (FilterMode) (Object) minFilter,
                (FilterMode) (Object) magFilter,
                maxAnisotropy,
                maxLod
        );
    }

    @Override
    public IGpuTexture ph$createTexture(@Nullable Supplier<String> supplier, @TextureUsage int usage, TextureFormat textureFormat, Vector3ic size, int mipLevels) {
        GlTexture texture = GlTexture.createTexture(supplier != null ? supplier.get() : null, usage, textureFormat, size, mipLevels);

        try (var state = texture.createStateAccess()) {
            state.texParameter(GL12.GL_TEXTURE_MIN_LOD, 0);
            state.texParameter(GL12.GL_TEXTURE_MAX_LOD, mipLevels - 1);
            state.texParameter(GL12.GL_TEXTURE_MAX_LEVEL, mipLevels - 1);

            for (int layer = 0; layer < texture.ph$getLayers(); layer++) {
                for (int mip = 0; mip < mipLevels; mip++)
                    state.texImage(layer, mip, texture.ph$getSize(mipLevels), null);
            }
        }

        ((GlDebugLabelsExt) debugLabels).applyLabel(texture);
        return texture;
    }

    @Override
    public IGpuBuffer ph$createBuffer(@Nullable Supplier<String> supplier, @BufferUsage int usage, long byteSize) {
        return (IGpuBuffer) createBuffer(supplier, usage, byteSize);
    }

    @Override
    public IGpuBuffer ph$createBuffer(@Nullable Supplier<String> supplier, @BufferUsage int usage, ByteBuffer contents) {
        return (IGpuBuffer) createBuffer(supplier, usage, contents);
    }
}
