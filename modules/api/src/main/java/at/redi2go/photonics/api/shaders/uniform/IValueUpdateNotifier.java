package at.redi2go.photonics.api.shaders.uniform;

@FunctionalInterface
public interface IValueUpdateNotifier {
    void setListener(Runnable var1);
}
