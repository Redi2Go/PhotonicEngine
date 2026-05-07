package at.redi2go.photonics.core.iris.pipeline.uniform;

public interface IUniformUpdateFrequency {
    static IUniformUpdateFrequency once() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IUniformUpdateFrequency perTick() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IUniformUpdateFrequency perFrame() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IUniformUpdateFrequency custom() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
