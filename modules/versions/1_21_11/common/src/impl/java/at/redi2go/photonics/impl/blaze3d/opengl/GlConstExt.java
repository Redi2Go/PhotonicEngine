package at.redi2go.photonics.impl.blaze3d.opengl;

import at.redi2go.photonics.game.blaze3d.textures.TexelFormat;
import at.redi2go.photonics.game.blaze3d.textures.TexelType;
import at.redi2go.photonics.game.blaze3d.textures.TextureFormat;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL41;

public class GlConstExt {
    public static int toGlInternalId(TextureFormat format) {
        return switch (format) {
            case RGBA -> GL11.GL_RGBA;
            case R8 -> GL30.GL_R8;
            case RG8 -> GL30.GL_RG8;
            case RGB8 -> GL11.GL_RGB8;
            case RGBA8 -> GL11.GL_RGBA8;
            case R8_SNORM -> GL31.GL_R8_SNORM;
            case RG8_SNORM -> GL31.GL_RG8_SNORM;
            case RGB8_SNORM -> GL31.GL_RGB8_SNORM;
            case RGBA8_SNORM -> GL31.GL_RGBA8_SNORM;
            case R16 -> GL30.GL_R16;
            case RG16 -> GL30.GL_RG16;
            case RGB16 -> GL11.GL_RGB16;
            case RGBA16 -> GL11.GL_RGBA16;
            case R16_SNORM -> GL31.GL_R16_SNORM;
            case RG16_SNORM -> GL31.GL_RG16_SNORM;
            case RGB16_SNORM -> GL31.GL_RGB16_SNORM;
            case RGBA16_SNORM -> GL31.GL_RGBA16_SNORM;
            case R16F -> GL30.GL_R16F;
            case RG16F -> GL30.GL_RG16F;
            case RGB16F -> GL30.GL_RGB16F;
            case RGBA16F -> GL30.GL_RGBA16F;
            case R32F -> GL30.GL_R32F;
            case RG32F -> GL30.GL_RG32F;
            case RGB32F -> GL30.GL_RGB32F;
            case RGBA32F -> GL30.GL_RGBA32F;
            case R8I -> GL30.GL_R8I;
            case RG8I -> GL30.GL_RG8I;
            case RGB8I -> GL30.GL_RGB8I;
            case RGBA8I -> GL30.GL_RGBA8I;
            case R8UI -> GL30.GL_R8UI;
            case RG8UI -> GL30.GL_RG8UI;
            case RGB8UI -> GL30.GL_RGB8UI;
            case RGBA8UI -> GL30.GL_RGBA8UI;
            case R16I -> GL30.GL_R16I;
            case RG16I -> GL30.GL_RG16I;
            case RGB16I -> GL30.GL_RGB16I;
            case RGBA16I -> GL30.GL_RGBA16I;
            case R16UI -> GL30.GL_R16UI;
            case RG16UI -> GL30.GL_RG16UI;
            case RGB16UI -> GL30.GL_RGB16UI;
            case RGBA16UI -> GL30.GL_RGBA16UI;
            case R32I -> GL30.GL_R32I;
            case RG32I -> GL30.GL_RG32I;
            case RGB32I -> GL30.GL_RGB32I;
            case RGBA32I -> GL30.GL_RGBA32I;
            case R32UI -> GL30.GL_R32UI;
            case RG32UI -> GL30.GL_RG32UI;
            case RGB32UI -> GL30.GL_RGB32UI;
            case RGBA32UI -> GL30.GL_RGBA32UI;
            case RGBA4 -> GL11.GL_RGBA4;
            case R3_G3_B2 -> GL11.GL_R3_G3_B2;
            case RGB5_A1 -> GL11.GL_RGB5_A1;
            case RGB565 -> GL41.GL_RGB565;
            case RGB10_A2 -> GL11.GL_RGB10_A2;
            case RGB10_A2UI -> GL33.GL_RGB10_A2UI;
            case R11F_G11F_B10F -> GL30.GL_R11F_G11F_B10F;
            case RGB9_E5 -> GL30.GL_RGB9_E5;
        };
    }

    public static int toGlTexelFormatId(TexelFormat format) {
        return switch (format) {
            case RED -> GL11.GL_RED;
            case RG -> GL30.GL_RG;
            case RGB -> GL11.GL_RGB;
            case RGBA -> GL11.GL_RGBA;
            case RED_INTEGER -> GL30.GL_RED_INTEGER;
            case RG_INTEGER -> GL30.GL_RG_INTEGER;
            case RGB_INTEGER -> GL30.GL_RGB_INTEGER;
            case RGBA_INTEGER -> GL30.GL_RGBA_INTEGER;
        };
    }

    public static int toGlTexelTypeId(TexelType type) {
        return switch (type) {
            case UNORM_BYTE, UNSIGNED_BYTE -> GL11.GL_UNSIGNED_BYTE;
            case UNORM_SHORT, UNSIGNED_SHORT -> GL11.GL_UNSIGNED_SHORT;
            case UNORM_INT, UNSIGNED_INT -> GL11.GL_UNSIGNED_INT;
            case SNORM_BYTE, BYTE -> GL11.GL_BYTE;
            case SNORM_SHORT, SHORT -> GL11.GL_SHORT;
            case SNORM_INT, INT -> GL11.GL_INT;
            case HALF_FLOAT -> GL30.GL_HALF_FLOAT;
            case FLOAT -> GL11.GL_FLOAT;
            case UNSIGNED_BYTE_3_3_2 -> GL12.GL_UNSIGNED_BYTE_3_3_2;
            case UNSIGNED_BYTE_2_3_3_REV -> GL12.GL_UNSIGNED_BYTE_2_3_3_REV;
            case UNSIGNED_SHORT_5_6_5 -> GL12.GL_UNSIGNED_SHORT_5_6_5;
            case UNSIGNED_SHORT_5_6_5_REV -> GL12.GL_UNSIGNED_SHORT_5_6_5_REV;
            case UNSIGNED_SHORT_4_4_4_4 -> GL12.GL_UNSIGNED_SHORT_4_4_4_4;
            case UNSIGNED_SHORT_4_4_4_4_REV -> GL12.GL_UNSIGNED_SHORT_4_4_4_4_REV;
            case UNSIGNED_SHORT_5_5_5_1 -> GL12.GL_UNSIGNED_SHORT_5_5_5_1;
            case UNSIGNED_SHORT_1_5_5_5_REV -> GL12.GL_UNSIGNED_SHORT_1_5_5_5_REV;
            case UNSIGNED_INT_8_8_8_8 -> GL12.GL_UNSIGNED_INT_8_8_8_8;
            case UNSIGNED_INT_8_8_8_8_REV -> GL12.GL_UNSIGNED_INT_8_8_8_8_REV;
            case UNSIGNED_INT_10_10_10_2 -> GL12.GL_UNSIGNED_INT_10_10_10_2;
            case UNSIGNED_INT_2_10_10_10_REV -> GL12.GL_UNSIGNED_INT_2_10_10_10_REV;
            case UNSIGNED_INT_10F_11F_11F_REV -> GL30.GL_UNSIGNED_INT_10F_11F_11F_REV;
            case UNSIGNED_INT_5_9_9_9_REV -> GL30.GL_UNSIGNED_INT_5_9_9_9_REV;
        };
    }
    
    private GlConstExt() { }
}
