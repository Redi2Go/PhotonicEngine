package at.redi2go.photonics.core.config.lights;

public interface LightsProvider {
    void registerLights(LightRegistry lights);

    void registerChangeListener(Runnable consumer);

    void clearListeners();
}
