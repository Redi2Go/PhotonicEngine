package at.redi2go.photonics.core.rendering.world.compiler;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.mc.Minecraft;
import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.rendering.world.BlockRegistry;
import at.redi2go.photonics.core.rendering.world.WorldOrigin;
import at.redi2go.photonics.core.rendering.world.registry.buffer.BufferBlockRegistry;
import at.redi2go.photonics.core.rendering.world.tree.ChunkVoxel;
import at.redi2go.photonics.core.rendering.world.tree.WorldVoxel;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntSupplier;

public class WorldCompiler implements Runnable, Disposable {
    public static final int MAX_SECTIONS_PER_RUN = 12;

    private static final int THREAD_POOL_SIZE = 3;
    private static final ExecutorService THREAD_POOL;

    private final IntSupplier renderDistanceSupplier;
    private final SectionManager sectionManager;
    private final BlockRegistry registry;

    private final Queue<WorldVoxel> uploadQueue;
    private final WorldVoxel rootVoxel;
    private final Set<ChunkVoxel> chunks = new HashSet<>();

    private final ReentrantLock uploadLock = new ReentrantLock();
    private final Condition uploadDone = uploadLock.newCondition();
    private boolean waitingForUpload = false;
    private boolean movingCenter = true;

    private Vector3i iorigin = null;
    private WorldOrigin origin = null;

    private WorldOrigin mostRecentOrigin;

    private final Thread compilerThread;

    public WorldCompiler(
            int depth,
            IntSupplier renderDistanceSupplier,
            SectionManager sectionManager,
            BlockRegistry blockRegistry,
            IGpuBufferHeap heap
    ) {
        this.renderDistanceSupplier = renderDistanceSupplier;
        this.sectionManager = sectionManager;
        this.registry = blockRegistry;

        this.uploadQueue = new ConcurrentLinkedQueue<>();
        this.rootVoxel = WorldVoxel.create(depth, this, registry, heap, uploadQueue);

        this.compilerThread = new Thread(this, "Photonics World Compiler");
        this.compilerThread.start();
    }

    private void setOrigin(Vector3i origin) {
        this.iorigin = origin;
        this.origin = new WorldOrigin(origin.x, origin.y, origin.z);
    }

    public WorldOrigin origin() {
        return mostRecentOrigin;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                unloadSections();
                rootVoxel.pruneEmptyVoxels();

                registry.freeUnusedBlocks();

                var sections = sectionManager.builtSections().drain(MAX_SECTIONS_PER_RUN);
                recenter();

                clearPendingSections(sections);

                voxelizeSections(sections);
                buildSections();

                awaitUpload();
            }
        } catch (InterruptedException e) {

        }
    }

    // Compiler Steps

    private void unloadSections() {
        if (iorigin == null) return;

        var unloadQueue = sectionManager.worldUnloadedSections();
        while (!unloadQueue.isEmpty()) {
            var sectionCoord = unloadQueue.remove();
            Vector3i sectionVoxelPos = sectionCoord.mul(16, new Vector3i())
                    .sub(iorigin)
                    .mul(16);

            rootVoxel.removeChunk(
                    sectionVoxelPos.x,
                    sectionVoxelPos.y,
                    sectionVoxelPos.z,
                    toRegion(sectionCoord)
            );
        }
    }

    private void recenter() throws InterruptedException {
        var newOrigin = getWorldOrigin(Minecraft.getCameraPos(), renderDistanceSupplier.getAsInt() + 3);
        if (iorigin == null) {
            setOrigin(newOrigin);
            return;
        }

        if (iorigin.equals(newOrigin)) return;

        uploadLock.lockInterruptibly();

        try {
            movingCenter = true;
        } finally {
            uploadLock.unlock();
        }

        var chunks = new ArrayList<>(this.chunks);
        for (var chunk : chunks)
            rootVoxel.removeChunkUnsafe(chunk.x(), chunk.y(), chunk.z());

        var offset = iorigin.sub(newOrigin, new Vector3i());
        offset.x = offset.x << 4;
        offset.y = offset.y << 4;
        offset.z = offset.z << 4;

        for (var chunk : chunks) {
            int newX = chunk.x() + offset.x;
            int newY = chunk.y() + offset.y;
            int newZ = chunk.z() + offset.z;

            if (!rootVoxel.containsChunk(newX, newY, newZ)) {
                chunk.close();
                continue;
            }

            rootVoxel.insertChunk(newX, newY, newZ, chunk);
        }

        rootVoxel.pruneEmptyVoxels();

        setOrigin(newOrigin);
    }

    private void clearPendingSections(List<ChunkCompiler.BuildResult> sections) {
        ShortSet regions = new ShortOpenHashSet();
        for (var section : sections) regions.add(toRegion(section.chunkPos()));

        rootVoxel.removeRegions(regions);
    }

    private void voxelizeSections(List<ChunkCompiler.BuildResult> sections) {
        for (var builtSection : sections) {
            var chunkBlockPos = builtSection.chunkBlockPos();
            var sectionBakery = builtSection.bakery();

            chunkBlockPos.sub(iorigin);
            if (!rootVoxel.containsChunk(chunkBlockPos)) continue;

            sectionBakery.setRegion(toRegion(builtSection.chunkPos()));
            sectionBakery.setChunkOffset(chunkBlockPos);
            sectionBakery.bake(rootVoxel, rootVoxel);
        }
    }

    private void buildSections() throws InterruptedException {
        var task = new MultiThreadTask();

        while (!uploadQueue.isEmpty())
            task.queueJob(uploadQueue.remove()::upload);

        task.awaitPending();
    }

    // Uploading

    private void awaitUpload() throws InterruptedException {
        uploadLock.lockInterruptibly();

        try {
            waitingForUpload = true;
            uploadDone.await();
        } finally {
            uploadLock.unlock();
        }
    }

    public boolean clearUpload() {
        uploadLock.lock();

        try {
            if (movingCenter) {
                if (!waitingForUpload) return false;

                mostRecentOrigin = origin;
                movingCenter = false;
            }

            waitingForUpload = false;

            uploadDone.signalAll();
        } finally {
            uploadLock.unlock();
        }

        return true;
    }


    // Chunk management

    public void addChunk(ChunkVoxel chunk) {
        chunks.add(chunk);
    }

    public void removeChunk(ChunkVoxel chunk) {
        chunks.remove(chunk);
    }


    @Override
    public void close() {
        compilerThread.interrupt();
    }


    // Util

    private static short toRegion(Vector3i chunkPos) {
        int hash = chunkPos.hashCode();
        return (short) (hash ^ (hash >>> 16));
    }

    private static int snapToSectionPos(int component, int renderDistance) {
        return ((component >> 4) - renderDistance) << 4;
    }

    private static Vector3i getWorldOrigin(Vector3d cameraPos, int renderDistance) {
        return new Vector3i(
                snapToSectionPos((int) cameraPos.x, renderDistance),
                snapToSectionPos((int) cameraPos.y, renderDistance),
                snapToSectionPos((int) cameraPos.z, renderDistance)
        );
    }


    // Tasks

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
