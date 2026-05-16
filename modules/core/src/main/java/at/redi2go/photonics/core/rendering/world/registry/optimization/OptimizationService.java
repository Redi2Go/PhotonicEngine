package at.redi2go.photonics.core.rendering.world.registry.optimization;

import at.redi2go.photonics.core.rendering.RenderingComponent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class OptimizationService implements RenderingComponent {
    private static final int OPTIMIZATION_THREAD_COUNT = 1;

    private final ExecutorService executor;

    private final Queue<Runnable> uploadQueue = new ConcurrentLinkedQueue<>();
    private long lastUpload = System.currentTimeMillis();

    public OptimizationService() {
        AtomicInteger threadCount = new AtomicInteger();
        executor = Executors.newFixedThreadPool(
                OPTIMIZATION_THREAD_COUNT,
                (r) -> new Thread(r, "Photonics Optimization Thread #" + threadCount.incrementAndGet())
        );
    }

    @Override
    public void onFrameBegin() {
        long time = System.currentTimeMillis();
        if ((time - lastUpload) < 500) return;

        lastUpload = time;
        while (!uploadQueue.isEmpty())
            uploadQueue.remove().run();
    }

    public void scheduleOptimization(Runnable runnable) {
        try {
            executor.execute(runnable);
        } catch (RejectedExecutionException e) {
            // Nothing
        }
    }

    public void scheduleUpload(Runnable runnable) {
        uploadQueue.add(runnable);
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
