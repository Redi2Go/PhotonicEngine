package at.redi2go.photonics.game.minecraft;

public interface IIdentifier {
    String ph$namespace();

    String ph$path();

    static IIdentifier fromNamespaceAndPath(String namespace, String path) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IIdentifier parse(String string) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IIdentifier withDefaultNamespace(String path) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
