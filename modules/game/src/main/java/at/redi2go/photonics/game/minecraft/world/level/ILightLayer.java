package at.redi2go.photonics.game.minecraft.world.level;

public interface ILightLayer {
    static ILightLayer sky() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static ILightLayer block() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN

    }
}
