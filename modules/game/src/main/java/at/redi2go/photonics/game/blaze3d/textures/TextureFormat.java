package at.redi2go.photonics.game.blaze3d.textures;

public enum TextureFormat {
    RGBA(TexelFormat.RGBA, TexelType.UNORM_BYTE),
    R8(TexelFormat.RED, TexelType.UNORM_BYTE),
    RG8(TexelFormat.RG, TexelType.UNORM_BYTE),
    RGB8(TexelFormat.RGB, TexelType.UNORM_BYTE),
    RGBA8(TexelFormat.RGBA, TexelType.UNORM_BYTE),
    R8_SNORM(TexelFormat.RED, TexelType.SNORM_BYTE),
    RG8_SNORM(TexelFormat.RG, TexelType.SNORM_BYTE),
    RGB8_SNORM(TexelFormat.RGB, TexelType.SNORM_BYTE),
    RGBA8_SNORM(TexelFormat.RGBA, TexelType.SNORM_BYTE),
    R16(TexelFormat.RED, TexelType.UNORM_SHORT),
    RG16(TexelFormat.RG, TexelType.UNORM_SHORT),
    RGB16(TexelFormat.RGB, TexelType.UNORM_SHORT),
    RGBA16(TexelFormat.RGBA, TexelType.UNORM_SHORT),
    R16_SNORM(TexelFormat.RED, TexelType.SNORM_SHORT),
    RG16_SNORM(TexelFormat.RG, TexelType.SNORM_SHORT),
    RGB16_SNORM(TexelFormat.RGB, TexelType.SNORM_SHORT),
    RGBA16_SNORM(TexelFormat.RGBA, TexelType.SNORM_SHORT),
    R16F(TexelFormat.RED, TexelType.HALF_FLOAT),
    RG16F(TexelFormat.RG, TexelType.HALF_FLOAT),
    RGB16F(TexelFormat.RGB, TexelType.HALF_FLOAT),
    RGBA16F(TexelFormat.RGBA, TexelType.HALF_FLOAT),
    R32F(TexelFormat.RED, TexelType.FLOAT),
    RG32F(TexelFormat.RG, TexelType.FLOAT),
    RGB32F(TexelFormat.RGB, TexelType.FLOAT),
    RGBA32F(TexelFormat.RGBA, TexelType.FLOAT),
    R8I(TexelFormat.RED_INTEGER, TexelType.BYTE),
    RG8I(TexelFormat.RG_INTEGER, TexelType.BYTE),
    RGB8I(TexelFormat.RGB_INTEGER, TexelType.BYTE),
    RGBA8I(TexelFormat.RGBA_INTEGER, TexelType.BYTE),
    R8UI(TexelFormat.RED_INTEGER, TexelType.UNSIGNED_BYTE),
    RG8UI(TexelFormat.RG_INTEGER, TexelType.UNSIGNED_BYTE),
    RGB8UI(TexelFormat.RGB_INTEGER, TexelType.UNSIGNED_BYTE),
    RGBA8UI(TexelFormat.RGBA_INTEGER, TexelType.UNSIGNED_BYTE),
    R16I(TexelFormat.RED_INTEGER, TexelType.SHORT),
    RG16I(TexelFormat.RG_INTEGER, TexelType.SHORT),
    RGB16I(TexelFormat.RGB_INTEGER, TexelType.SHORT),
    RGBA16I(TexelFormat.RGBA_INTEGER, TexelType.SHORT),
    R16UI(TexelFormat.RED_INTEGER, TexelType.UNSIGNED_SHORT),
    RG16UI(TexelFormat.RG_INTEGER, TexelType.UNSIGNED_SHORT),
    RGB16UI(TexelFormat.RGB_INTEGER, TexelType.UNSIGNED_SHORT),
    RGBA16UI(TexelFormat.RGBA_INTEGER, TexelType.UNSIGNED_SHORT),
    R32I(TexelFormat.RED_INTEGER, TexelType.INT),
    RG32I(TexelFormat.RG_INTEGER, TexelType.INT),
    RGB32I(TexelFormat.RGB_INTEGER, TexelType.INT),
    RGBA32I(TexelFormat.RGBA_INTEGER, TexelType.INT),
    R32UI(TexelFormat.RED_INTEGER, TexelType.UNSIGNED_INT),
    RG32UI(TexelFormat.RG_INTEGER, TexelType.UNSIGNED_INT),
    RGB32UI(TexelFormat.RGB_INTEGER, TexelType.UNSIGNED_INT),
    RGBA32UI(TexelFormat.RGBA_INTEGER, TexelType.UNSIGNED_INT),
    RGBA4(TexelFormat.RGBA, TexelType.UNSIGNED_SHORT_4_4_4_4_REV),
    R3_G3_B2(TexelFormat.RGB, TexelType.UNSIGNED_BYTE_2_3_3_REV),
    RGB5_A1(TexelFormat.RGBA, TexelType.UNSIGNED_SHORT_1_5_5_5_REV),
    RGB565(TexelFormat.RGB, TexelType.UNSIGNED_SHORT_5_6_5_REV),
    RGB10_A2(TexelFormat.RGBA, TexelType.UNSIGNED_INT_2_10_10_10_REV),
    RGB10_A2UI(TexelFormat.RGBA_INTEGER, TexelType.UNSIGNED_INT_2_10_10_10_REV),
    R11F_G11F_B10F(TexelFormat.RGB, TexelType.UNSIGNED_INT_10F_11F_11F_REV),
    RGB9_E5(TexelFormat.RGB, TexelType.UNSIGNED_INT_5_9_9_9_REV);

    private final TexelFormat texelFormat;
    private final TexelType texelType;

    private final int texelByteSize;

    TextureFormat(TexelFormat texelFormat, TexelType texelType) {
        this.texelFormat = texelFormat;
        this.texelType = texelType;

        this.texelByteSize = texelFormat.getComponentCount() * texelType.getByteSize();
    }

    public TexelFormat getTexelFormat() {
        return texelFormat;
    }

    public TexelType getTexelType() {
        return texelType;
    }

    public int getTexelByteSize() {
        return texelByteSize;
    }

    public boolean supportsWrite() {
        return texelFormat.supportsWrite() && texelType.supportsWrite();
    }

    public boolean supportsImage() {
        return texelFormat.supportsImage() && texelType.supportsImage();
    }
}
