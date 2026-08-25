package at.redi2go.photonics.game.blaze3d.systems;

public interface IRenderSystem {
    static IGpuDevice getDevice() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
