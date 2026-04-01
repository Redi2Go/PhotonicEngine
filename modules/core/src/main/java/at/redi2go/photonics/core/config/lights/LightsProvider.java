package at.redi2go.photonics.core.config.lights;

public interface LightsProvider {
    void registerLights(LightList lights);

    void registerChangeListener(Runnable consumer);

    void clearListeners();
}
