package at.redi2go.photonics.api.gpu.textures;

public interface ITextureFormat {
    int pixelSize();

    static ITextureFormat rgba32f() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static ITextureFormat rgba16f() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static ITextureFormat rgb32f() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static ITextureFormat r16f() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }


    // From blaze3d
    static ITextureFormat rgba8() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static ITextureFormat red8() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static ITextureFormat red8i() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static ITextureFormat depth32() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
