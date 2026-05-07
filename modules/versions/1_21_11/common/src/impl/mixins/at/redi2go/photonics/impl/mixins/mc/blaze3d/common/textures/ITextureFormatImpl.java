package at.redi2go.photonics.impl.mixins.mc.blaze3d.common.textures;

import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import at.redi2go.photonics.impl.mc.blaze3d.common.GpuDeviceImpl;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ITextureFormat.class)
public interface ITextureFormatImpl {
    @Overwrite
	static ITextureFormat rgba() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgba();
	}

    @Overwrite
	static ITextureFormat r8() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().r8();
	}

    @Overwrite
	static ITextureFormat rg8() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rg8();
	}

    @Overwrite
	static ITextureFormat rgb8() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgb8();
	}

    @Overwrite
	static ITextureFormat rgba8() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgba8();
	}

    @Overwrite
	static ITextureFormat r8Snorm() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().r8Snorm();
	}

    @Overwrite
	static ITextureFormat rg8Snorm() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rg8Snorm();
	}

    @Overwrite
	static ITextureFormat rgb8Snorm() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgb8Snorm();
	}

    @Overwrite
	static ITextureFormat rgba8Snorm() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgba8Snorm();
	}

    @Overwrite
	static ITextureFormat r16() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().r16();
	}

    @Overwrite
	static ITextureFormat rg16() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rg16();
	}

    @Overwrite
	static ITextureFormat rgb16() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgb16();
	}

    @Overwrite
	static ITextureFormat rgba16() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgba16();
	}

    @Overwrite
	static ITextureFormat r16Snorm() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().r16Snorm();
	}

    @Overwrite
	static ITextureFormat rg16Snorm() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rg16Snorm();
	}

    @Overwrite
	static ITextureFormat rgb16Snorm() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgb16Snorm();
	}

    @Overwrite
	static ITextureFormat rgba16Snorm() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgba16Snorm();
	}

    @Overwrite
	static ITextureFormat r16f() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().r16f();
	}

    @Overwrite
	static ITextureFormat rg16f() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rg16f();
	}

    @Overwrite
	static ITextureFormat rgb16f() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgb16f();
	}

    @Overwrite
	static ITextureFormat rgba16f() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgba16f();
	}

    @Overwrite
	static ITextureFormat r32f() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().r32f();
	}

    @Overwrite
	static ITextureFormat rg32f() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rg32f();
	}

    @Overwrite
	static ITextureFormat rgb32f() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgb32f();
	}

    @Overwrite
	static ITextureFormat rgba32f() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgba32f();
	}

    @Overwrite
	static ITextureFormat r8i() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().r8i();
	}

    @Overwrite
	static ITextureFormat rg8i() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rg8i();
	}

    @Overwrite
	static ITextureFormat rgb8i() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgb8i();
	}

    @Overwrite
	static ITextureFormat rgba8i() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgba8i();
	}

    @Overwrite
	static ITextureFormat r8ui() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().r8ui();
	}

    @Overwrite
	static ITextureFormat rg8ui() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rg8ui();
	}

    @Overwrite
	static ITextureFormat rgb8ui() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgb8ui();
	}

    @Overwrite
	static ITextureFormat rgba8ui() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgba8ui();
	}

    @Overwrite
	static ITextureFormat r16i() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().r16i();
	}

    @Overwrite
	static ITextureFormat rg16i() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rg16i();
	}

    @Overwrite
	static ITextureFormat rgb16i() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgb16i();
	}

    @Overwrite
	static ITextureFormat rgba16i() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgba16i();
	}

    @Overwrite
	static ITextureFormat r16ui() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().r16ui();
	}

    @Overwrite
	static ITextureFormat rg16ui() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rg16ui();
	}

    @Overwrite
	static ITextureFormat rgb16ui() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgb16ui();
	}

    @Overwrite
	static ITextureFormat rgba16ui() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgba16ui();
	}

    @Overwrite
	static ITextureFormat r32i() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().r32i();
	}

    @Overwrite
	static ITextureFormat rg32i() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rg32i();
	}

    @Overwrite
	static ITextureFormat rgb32i() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgb32i();
	}

    @Overwrite
	static ITextureFormat rgba32i() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgba32i();
	}

    @Overwrite
	static ITextureFormat r32ui() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().r32ui();
	}

    @Overwrite
	static ITextureFormat rg32ui() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rg32ui();
	}

    @Overwrite
	static ITextureFormat rgb32ui() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgb32ui();
	}

    @Overwrite
	static ITextureFormat rgba32ui() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgba32ui();
	}

    @Overwrite
	static ITextureFormat rgba2() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgba2();
	}

    @Overwrite
	static ITextureFormat rgba4() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgba4();
	}

    @Overwrite
	static ITextureFormat r3g3b2() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().r3g3b2();
	}

    @Overwrite
	static ITextureFormat rgb5a1() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgb5a1();
	}

    @Overwrite
	static ITextureFormat rgb565() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgb565();
	}

    @Overwrite
	static ITextureFormat rgb10a2() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgb10a2();
	}

    @Overwrite
	static ITextureFormat rgb10A2ui() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgb10A2ui();
	}

    @Overwrite
	static ITextureFormat r11fg11fb10f() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().r11fg11fb10f();
	}

    @Overwrite
	static ITextureFormat rgb9e5() {
		return (ITextureFormat) ((GpuDeviceImpl) RenderSystem.getDevice()).getTextureFormats().rgb9e5();
	}
}
