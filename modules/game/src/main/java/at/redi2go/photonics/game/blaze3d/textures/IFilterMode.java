package at.redi2go.photonics.game.blaze3d.textures;

public interface IFilterMode {
    static IFilterMode nearest() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IFilterMode linear() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
