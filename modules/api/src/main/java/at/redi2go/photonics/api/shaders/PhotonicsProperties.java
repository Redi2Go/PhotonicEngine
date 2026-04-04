package at.redi2go.photonics.api.shaders;

public interface PhotonicsProperties {
    boolean isPhotonicsEnabled();
    boolean DEFAULT_PHOTONICS_ENABLED = false;
    String PHOTONICS_ENABLED_KEY = "photonics.enabled";
}
