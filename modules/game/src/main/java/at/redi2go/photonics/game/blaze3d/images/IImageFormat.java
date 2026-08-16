package at.redi2go.photonics.game.blaze3d.images;

import at.redi2go.photonics.game.blaze3d.textures.ITextureFormat;

public interface IImageFormat {
    ITextureFormat ph$toTextureFormat();

    IPixelFormat  ph$getPixelFormat();

    IPixelType ph$getPixelType();

    boolean isNormalized();

    default int ph$getColorByteSize() {
        return ph$getPixelFormat().ph$getComponentCount() * ph$getPixelType().ph$getByteSize();
    }

    static IImageFormat r8() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rg8() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rgba8() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat r8Snorm() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rg8Snorm() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rgba8Snorm() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat r16() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rg16() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rgba16() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat r16Snorm() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rg16Snorm() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rgba16Snorm() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat r16f() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rg16f() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rgba16f() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat r32f() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rg32f() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rgba32f() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat r8i() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rg8i() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rgba8i() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat r8ui() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rg8ui() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rgba8ui() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat r16i() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rg16i() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rgba16i() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat r16ui() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rg16ui() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rgba16ui() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat r32i() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rg32i() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rgba32i() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat r32ui() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rg32ui() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IImageFormat rgba32ui() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
