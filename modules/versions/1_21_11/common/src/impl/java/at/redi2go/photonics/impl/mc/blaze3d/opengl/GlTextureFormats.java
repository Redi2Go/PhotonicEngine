package at.redi2go.photonics.impl.mc.blaze3d.opengl;

import at.redi2go.photonics.impl.mc.blaze3d.common.TextureFormats;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;

public class GlTextureFormats implements TextureFormats {
    public static final GlTextureFormats INSTANCE = new GlTextureFormats();

    private GlTextureFormats() {}

    @Override
    public Object rgba() {
        return InternalTextureFormat.RGBA;
    }

    @Override
    public Object r8() {
        return InternalTextureFormat.R8;
    }

    @Override
    public Object rg8() {
        return InternalTextureFormat.RG8;
    }

    @Override
    public Object rgb8() {
        return InternalTextureFormat.RGB8;
    }

    @Override
    public Object rgba8() {
        return InternalTextureFormat.RGBA8;
    }

    @Override
    public Object r8Snorm() {
        return InternalTextureFormat.R8_SNORM;
    }

    @Override
    public Object rg8Snorm() {
        return InternalTextureFormat.RG8_SNORM;
    }

    @Override
    public Object rgb8Snorm() {
        return InternalTextureFormat.RGB8_SNORM;
    }

    @Override
    public Object rgba8Snorm() {
        return InternalTextureFormat.RGBA8_SNORM;
    }

    @Override
    public Object r16() {
        return InternalTextureFormat.R16;
    }

    @Override
    public Object rg16() {
        return InternalTextureFormat.RG16;
    }

    @Override
    public Object rgb16() {
        return InternalTextureFormat.RGB16;
    }

    @Override
    public Object rgba16() {
        return InternalTextureFormat.RGBA16;
    }

    @Override
    public Object r16Snorm() {
        return InternalTextureFormat.R16_SNORM;
    }

    @Override
    public Object rg16Snorm() {
        return InternalTextureFormat.RG16_SNORM;
    }

    @Override
    public Object rgb16Snorm() {
        return InternalTextureFormat.RGB16_SNORM;
    }

    @Override
    public Object rgba16Snorm() {
        return InternalTextureFormat.RGBA16_SNORM;
    }

    @Override
    public Object r16f() {
        return InternalTextureFormat.R16F;
    }

    @Override
    public Object rg16f() {
        return InternalTextureFormat.RGB16F;
    }

    @Override
    public Object rgb16f() {
        return InternalTextureFormat.RGB16F;
    }

    @Override
    public Object rgba16f() {
        return InternalTextureFormat.RGBA16F;
    }

    @Override
    public Object r32f() {
        return InternalTextureFormat.R32F;
    }

    @Override
    public Object rg32f() {
        return InternalTextureFormat.RG32F;
    }

    @Override
    public Object rgb32f() {
        return InternalTextureFormat.RGB32F;
    }

    @Override
    public Object rgba32f() {
        return InternalTextureFormat.RGBA32F;
    }

    @Override
    public Object r8i() {
        return InternalTextureFormat.R8I;
    }

    @Override
    public Object rg8i() {
        return InternalTextureFormat.RG8I;
    }

    @Override
    public Object rgb8i() {
        return InternalTextureFormat.RGB8I;
    }

    @Override
    public Object rgba8i() {
        return InternalTextureFormat.RGBA8I;
    }

    @Override
    public Object r8ui() {
        return InternalTextureFormat.R8UI;
    }

    @Override
    public Object rg8ui() {
        return InternalTextureFormat.RG8UI;
    }

    @Override
    public Object rgb8ui() {
        return InternalTextureFormat.RGB8UI;
    }

    @Override
    public Object rgba8ui() {
        return InternalTextureFormat.RGBA8UI;
    }

    @Override
    public Object r16i() {
        return InternalTextureFormat.R16I;
    }

    @Override
    public Object rg16i() {
        return InternalTextureFormat.RG16I;
    }

    @Override
    public Object rgb16i() {
        return InternalTextureFormat.RGB16I;
    }

    @Override
    public Object rgba16i() {
        return InternalTextureFormat.RGBA16I;
    }

    @Override
    public Object r16ui() {
        return InternalTextureFormat.R16UI;
    }

    @Override
    public Object rg16ui() {
        return InternalTextureFormat.RG16UI;
    }

    @Override
    public Object rgb16ui() {
        return InternalTextureFormat.RGB16UI;
    }

    @Override
    public Object rgba16ui() {
        return InternalTextureFormat.RGBA16UI;
    }

    @Override
    public Object r32i() {
        return InternalTextureFormat.R32I;
    }

    @Override
    public Object rg32i() {
        return InternalTextureFormat.RG32I;
    }

    @Override
    public Object rgb32i() {
        return InternalTextureFormat.RGB32I;
    }

    @Override
    public Object rgba32i() {
        return InternalTextureFormat.RGBA32I;
    }

    @Override
    public Object r32ui() {
        return InternalTextureFormat.R32UI;
    }

    @Override
    public Object rg32ui() {
        return InternalTextureFormat.RG32UI;
    }

    @Override
    public Object rgb32ui() {
        return InternalTextureFormat.RGB32UI;
    }

    @Override
    public Object rgba32ui() {
        return InternalTextureFormat.RGBA32UI;
    }

    @Override
    public Object rgba2() {
        return InternalTextureFormat.RGBA2;
    }

    @Override
    public Object rgba4() {
        return InternalTextureFormat.RGBA4;
    }

    @Override
    public Object r3g3b2() {
        return InternalTextureFormat.R3_G3_B2;
    }

    @Override
    public Object rgb5a1() {
        return InternalTextureFormat.RGB5_A1;
    }

    @Override
    public Object rgb565() {
        return InternalTextureFormat.RGB565;
    }

    @Override
    public Object rgb10a2() {
        return InternalTextureFormat.RGB10_A2;
    }

    @Override
    public Object rgb10A2ui() {
        return InternalTextureFormat.RGB10_A2UI;
    }

    @Override
    public Object r11fg11fb10f() {
        return InternalTextureFormat.R11F_G11F_B10F;
    }

    @Override
    public Object rgb9e5() {
        return InternalTextureFormat.RGB9_E5;
    }
}
