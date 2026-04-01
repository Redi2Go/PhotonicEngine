package at.redi2go.photonics.api.gpu.systems;

public interface IRenderSystem {
    static IGpuDevice getDevice() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
