package at.redi2go.photonics.api;

/**
 * An {@link AutoCloseable} that cannot throw an exception when closed.
 */
public interface Disposable extends AutoCloseable {
    @Override
    void close();
}
