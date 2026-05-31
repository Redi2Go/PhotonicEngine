package at.redi2go.photonics.core.old.world.compiler;

public interface CompilerTask {
    void queueJob(Runnable task);
}
