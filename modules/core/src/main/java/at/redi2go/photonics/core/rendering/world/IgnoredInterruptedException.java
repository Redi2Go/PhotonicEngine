package at.redi2go.photonics.core.rendering.world;

public class IgnoredInterruptedException extends RuntimeException {
    public static boolean shouldIgnore(Throwable t) {
        if (t == null) return true;

        while (t != null) {
            if (t instanceof IgnoredInterruptedException) return true;
            t = t.getCause();
        }

        return false;
    }
}
