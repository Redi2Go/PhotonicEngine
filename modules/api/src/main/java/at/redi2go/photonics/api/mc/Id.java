package at.redi2go.photonics.api.mc;

public interface Id {
    String namespace();

    String path();

    static Id fromNamespaceAndPath(String namespace, String path) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static Id parse(String string) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static Id withDefaultNamespace(String path) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
