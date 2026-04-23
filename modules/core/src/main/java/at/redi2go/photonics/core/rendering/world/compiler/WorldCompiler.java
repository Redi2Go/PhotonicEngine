package at.redi2go.photonics.core.rendering.world.compiler;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.mc.Minecraft;
import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.core.rendering.world.BlockRegistry;
import at.redi2go.photonics.core.rendering.world.WorldOrigin;
import at.redi2go.photonics.core.rendering.world.WorldVoxel;
import at.redi2go.photonics.core.rendering.world.bakery.BlockBakery;
import at.redi2go.photonics.core.rendering.world.bakery.BlockMesher;
import at.redi2go.photonics.core.rendering.world.bakery.impl.BlockBakeryImpl;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class WorldCompiler implements Runnable, Disposable {
    private static final int THREAD_POOL_SIZE = 3;
    private static final ExecutorService THREAD_POOL;

    private final Queue<WorldVoxel> uploadQueue;
    private final SectionQueue sectionQueue;

    private final ReentrantLock uploadLock = new ReentrantLock();
    private final Condition uploadDone = uploadLock.newCondition();
    private boolean waitingForUpload = false;

    private final WorldVoxel rootVoxel;
    private final BlockRegistry registry;

    private final Thread compilerThread;

    private WorldOrigin mostRecentOrigin;

    private final int renderDistance;

    private Vector3i iorigin = null;
    private WorldOrigin origin = null;

    private final ChunkCompiler chunkCompiler;

    public WorldCompiler(
            WorldVoxel rootVoxel,
            BlockRegistry registry,
            AtlasDownloader atlasDownloader,
            Queue<WorldVoxel> uploadQueue,
            int renderDistance
    ) {
        this.rootVoxel = rootVoxel;
        this.registry = registry;
        this.uploadQueue = uploadQueue;
        this.renderDistance = renderDistance;
        this.sectionQueue = new SectionQueue();

        this.chunkCompiler = new ChunkCompiler(atlasDownloader, registry, sectionQueue);

        this.compilerThread = new Thread(this, "Photonics World Compiler");
        this.compilerThread.start();
    }

    public WorldOrigin origin() {
        return mostRecentOrigin;
    }

    public int renderDistance() {
        return renderDistance;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                var sections = chunkCompiler.takeSections();

                recenter();

                voxelizeSections(sections);
                buildSections();

                awaitUpload();
            }
        } catch (InterruptedException e) {

        }
    }

    public void submitSection(Vector3i section) {
        sectionQueue.submitSection(section);
    }

    // compiler steps

    private void recenter() {
        if (origin != null) return;

        var cameraPos = Minecraft.getCameraPos();
        iorigin = new Vector3i(
                snapToSectionPos((int) cameraPos.x, renderDistance),
                snapToSectionPos((int) cameraPos.y, renderDistance),
                snapToSectionPos((int) cameraPos.z, renderDistance)
        );

        origin = new WorldOrigin(iorigin.x, iorigin.y, iorigin.z);
    }

    private void freeUnusedBlocks() {
        registry.freeUnusedBlocks();
    }

    private void clearPendingSections(List<Vector3i> section) {

    }

    private void voxelizeSections(List<Pair<Vector3i, BlockBakery>> sections) {
        for (var builtSection : sections) {
            var chunkPos = builtSection.first();
            var section = builtSection.second();

            if (section == null) continue;

            bake:
            {
                chunkPos.sub(iorigin);
                if (chunkPos.x < 0 || chunkPos.y < 0 || chunkPos.z < 0)
                    break bake;

                section.setChunkOffset(chunkPos);
                section.bake(rootVoxel, rootVoxel);
            }

            chunkCompiler.releaseBakery(section);
        }
    }

    private void buildSections() throws InterruptedException {
        var task = new MultiThreadTask();

        while (!uploadQueue.isEmpty())
            task.queueJob(uploadQueue.remove()::upload);

        task.awaitPending();
    }

    private void awaitUpload() throws InterruptedException {
        uploadLock.lockInterruptibly();

        try {
            waitingForUpload = true;
            uploadDone.await();
        } finally {
            uploadLock.unlock();
        }
    }

    public void clearUpload() {
        uploadLock.lock();

        try {
            if (waitingForUpload) {
                mostRecentOrigin = origin;
                waitingForUpload = false;
            }

            uploadDone.signalAll();
        } finally {
            uploadLock.unlock();
        }
    }


    @Override
    public void close() {
        compilerThread.interrupt();
        chunkCompiler.close();
    }

    private static int snapToSectionPos(int component, int renderDistance) {
        return ((component >> 4) - renderDistance) << 4;
    }

    private static class MultiThreadTask extends CompletableFuture<Void> implements CompilerTask {
        private final AtomicInteger PENDING_TASKS = new AtomicInteger();

        @Override
        public void queueJob(Runnable task) {
            PENDING_TASKS.incrementAndGet();

            THREAD_POOL.execute(() -> {
                try {
                    task.run();
                } finally {
                    if (PENDING_TASKS.decrementAndGet() == 0)
                        complete(null);
                }
            });
        }

        void awaitPending() throws InterruptedException {
            try {
                if (PENDING_TASKS.get() != 0)
                    get();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class SingleThreadTask implements CompilerTask {
        @Override
        public void queueJob(Runnable task) {
            task.run();
        }

        void awaitPending() {

        }
    }

    static {
        AtomicInteger count = new AtomicInteger(0);
        THREAD_POOL = Executors.newFixedThreadPool(THREAD_POOL_SIZE, (r) ->
                new Thread(r, "Photonics World Worker #" + count.getAndIncrement()));
    }
}
