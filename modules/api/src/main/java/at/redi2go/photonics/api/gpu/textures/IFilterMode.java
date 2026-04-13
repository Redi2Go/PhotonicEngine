package at.redi2go.photonics.api.gpu.textures;

public interface IFilterMode {
    static IFilterMode nearest() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IFilterMode linear() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
