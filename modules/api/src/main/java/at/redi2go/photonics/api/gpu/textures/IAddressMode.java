package at.redi2go.photonics.api.gpu.textures;

public interface IAddressMode {
    static IAddressMode repeat() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IAddressMode clampToEdge() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
