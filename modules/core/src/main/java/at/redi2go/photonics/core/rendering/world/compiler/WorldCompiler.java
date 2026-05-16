package at.redi2go.photonics.core.rendering.world.compiler;

import at.redi2go.photonics.api.mc.Minecraft;
import at.redi2go.photonics.core.iris.pipeline.uniform.IDynamicUniformHolder;
import at.redi2go.photonics.core.rendering.RenderingComponent;
import at.redi2go.photonics.core.rendering.SectionManager;
import at.redi2go.photonics.core.rendering.UniformUpdater;
import at.redi2go.photonics.core.rendering.WorldOrigin;
import at.redi2go.photonics.core.rendering.world.IgnoredInterruptedException;
import at.redi2go.photonics.core.rendering.world.allocator.WorldAllocator;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTexture;
import at.redi2go.photonics.core.rendering.world.registry.WorldRegistry;
import at.redi2go.photonics.core.rendering.world.tree.BlockMergeMode;
import at.redi2go.photonics.core.rendering.world.tree.ChunkManager;
import at.redi2go.photonics.core.rendering.world.tree.ChunkVoxel;
import at.redi2go.photonics.core.rendering.world.tree.WorldVoxel;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.joml.Vector3d;
import org.joml.Vector3f;
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

public class WorldCompiler implements ChunkManager, Runnable, RenderingComponent {
    public static final int MAX_SECTIONS_PER_RUN = 48;

    private static final int THREAD_POOL_SIZE = 3;
    private static final ExecutorService THREAD_POOL;

    private final SectionManager.TaskQueue<ChunkCompiler.BuildResult> taskQueue;

    private final WorldAllocator worldAllocator;
    private final PaletteTexture paletteTexture;

    private final WorldRegistry registry;

    private final Queue<WorldVoxel> uploadQueue;
    private final WorldVoxel rootVoxel;
    private final Set<ChunkVoxel> chunks = new HashSet<>();

    private final ReentrantLock uploadLock = new ReentrantLock();
    private final Condition uploadDone = uploadLock.newCondition();
    private boolean canUpload = true;

    private Vector3i iorigin = null;
    private WorldOrigin origin = null;

    private final Vector3i minVoxel = new Vector3i();
    private final Vector3i maxVoxel = new Vector3i();

    private final UniformUpdater uniformUpdater = new UniformUpdater();

    private WorldOrigin mostRecentOrigin;
    private Vector3f mostRecentMinVoxel = new Vector3f();
    private Vector3f mostRecentMaxVoxel = new Vector3f();

    private final Thread compilerThread;

    public WorldCompiler(
            int depth,
            WorldAllocator worldAllocator,
            PaletteTexture paletteTexture,
            SectionManager.TaskQueue<ChunkCompiler.BuildResult> taskQueue,
            WorldRegistry worldRegistry
    ) {
        this.worldAllocator = worldAllocator;
        this.paletteTexture = paletteTexture;

        this.taskQueue = taskQueue;
        this.registry = worldRegistry;

        this.uploadQueue = new ConcurrentLinkedQueue<>();
        this.rootVoxel = WorldVoxel.create(depth, BlockMergeMode.OVERWRITE, this, registry, uploadQueue);

        this.compilerThread = new Thread(this, "Photonics World Compiler");
        this.compilerThread.start();
    }

    public WorldOrigin origin() {
        return mostRecentOrigin;
    }

    private void setOrigin(Vector3i origin) {
        this.iorigin = origin;
        this.origin = new WorldOrigin(origin.x, origin.y, origin.z);
    }


    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                taskQueue.awaitTask();


                var unloadedSections = taskQueue.drainUnloadQueue();
                if (!unloadedSections.isEmpty()) {
                    clearUnloadedSections(unloadedSections);
                }


                var builtSections = taskQueue.drain(MAX_SECTIONS_PER_RUN);
                if (!builtSections.isEmpty()) {
                    recenter();

                    clearPendingSections(builtSections);
                    insertSections(builtSections);
                }

                if (!unloadedSections.isEmpty() || !builtSections.isEmpty()) {
                    rootVoxel.pruneEmptyVoxels();

                    stopUpload();
                    buildSections();
                    awaitUpload();

                    registry.objectManager().freeUnusedObjects();
                }
            }
        } catch (InterruptedException | IgnoredInterruptedException e) {

        }
    }


    // Compiler Steps

    private void clearUnloadedSections(List<Vector3i> unloadedSections) {
        if (iorigin == null) return;

        ShortSet regions = new ShortOpenHashSet(unloadedSections.size());
        for (var section : unloadedSections)
            regions.add(toRegion(section));

        rootVoxel.removeRegions(regions);
    }

    private void recenter() throws InterruptedException {
        var newOrigin = WorldOrigin.getAsVector3i();
        if (iorigin == null) {
            setOrigin(newOrigin);
            return;
        }

        if (iorigin.equals(newOrigin)) return;

        stopUpload();

        var chunks = new ArrayList<>(this.chunks);
        for (var chunk : chunks) {
            if (chunk == null) continue;

            rootVoxel.removeChunkUnsafe(chunk.x(), chunk.y(), chunk.z());
        }

        var offset = iorigin.sub(newOrigin, new Vector3i());
        offset.x = offset.x << 4;
        offset.y = offset.y << 4;
        offset.z = offset.z << 4;

        for (var chunk : chunks) {
            if (chunk == null) continue;

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
        ShortSet regions = new ShortOpenHashSet(sections.size());
        for (var section : sections)
            regions.add(toRegion(section.chunkPos()));

        rootVoxel.removeRegions(regions);
    }

    private void insertSections(List<ChunkCompiler.BuildResult> sections) {
        BlockSorter blockSorter = new BlockSorter();
        Vector3i blockVoxelPos = new Vector3i();

        for (var section : sections) {
            try (section) {
                blockSorter.reset();

                var chunkVoxelPos = new Vector3i(section.chunkBlockPos())
                        .sub(iorigin)
                        .mul(16);

                if (!rootVoxel.containsChunk(chunkVoxelPos)) continue;

                short region = toRegion(section.chunkPos());
                section.forEachBlock((blockChunkPos, block) -> blockSorter.addBlock(
                        chunkVoxelPos.add(blockChunkPos.mul(16), new Vector3i()),
                        block
                ));

                blockSorter.forEachBlock((block -> {
                    var parts = block.blockModel().parts();
                    for (int i = 0; i < parts.size(); i++) {
                        var part = parts.get(i);

                        blockVoxelPos.set(block.x(), block.y(), block.z());
                        blockVoxelPos.add(part.offset().mul(16, new Vector3i()));

                        rootVoxel.insertBlock(
                                blockVoxelPos.x, blockVoxelPos.y, blockVoxelPos.z,
                                region,
                                part.toEntry(region)
                        );
                    }
                }));
            }
        }
    }

    private void buildSections() throws InterruptedException {
        var task = new MultiThreadTask();

        var chunks = new ArrayList<>(this.chunks);

        while (!uploadQueue.isEmpty())
            task.queueJob(uploadQueue.remove()::upload);

        minVoxel.set(Integer.MAX_VALUE);
        maxVoxel.set(Integer.MIN_VALUE);

        var temp = new Vector3i();

        for (var chunk : chunks) {
            if (chunk == null) continue;

            temp.set(chunk.x(), chunk.y(), chunk.z());
            minVoxel.min(temp);

            temp.add(chunk.voxelSize());
            maxVoxel.max(temp);
        }

        task.awaitPending();
    }


    // Uploading

    private void stopUpload() throws InterruptedException {
        uploadLock.lockInterruptibly();

        try {
            canUpload = false;
        } finally {
            uploadLock.unlock();
        }
    }

    private void awaitUpload() throws InterruptedException {
        uploadLock.lockInterruptibly();

        try {
            canUpload = true;
            uniformUpdater.updateNextFrame();
            uploadDone.await();
        } finally {
            uploadLock.unlock();
        }
    }

    @Override
    public void onFrameBegin() {
        uploadLock.lock();

        try {
            if (!canUpload) return;

            worldAllocator.upload();
            paletteTexture.upload();
            uniformUpdater.updateAll();

            mostRecentOrigin = origin;

            mostRecentMinVoxel = new Vector3f(minVoxel);
            mostRecentMaxVoxel = new Vector3f(maxVoxel);

            uploadDone.signalAll();
        } finally {
            uploadLock.unlock();
        }
    }

    @Override
    public void registerDynamicUniforms(IDynamicUniformHolder dynamicUniforms) {
        dynamicUniforms.uniform3f(
                "world_offset",
                () -> {
                    var offset = mostRecentOrigin;
                    if (offset == null) return new Vector3f(0f);

                    return new Vector3f(offset);
                },
                uniformUpdater.newNotifier()
        );

        dynamicUniforms.uniform3f("world_min_voxel", () -> mostRecentMinVoxel, uniformUpdater.newNotifier());
        dynamicUniforms.uniform3f("world_max_voxel", () -> mostRecentMaxVoxel, uniformUpdater.newNotifier());

        dynamicUniforms.uniform3f("rt_camera_position", () -> {
            var offset = mostRecentOrigin;
            if (offset == null) return new Vector3f(0f);

            var pos = Minecraft.getCameraPos();
            return new Vector3f(offset.applyOffset(new Vector3d(pos.x, pos.y, pos.z)));
        }, uniformUpdater.newNotifier());
    }

    // Chunk management

    @Override
    public void addChunk(ChunkVoxel chunk) {
        chunks.add(chunk);
    }

    @Override
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


    // Tasks

    private static class MultiThreadTask extends CompletableFuture<Void> implements CompilerTask {
        private final AtomicInteger pendingTasks = new AtomicInteger();

        @Override
        public void queueJob(Runnable task) {
            pendingTasks.incrementAndGet();

            THREAD_POOL.execute(() -> {
                try {
                    task.run();
                } finally {
                    if (pendingTasks.decrementAndGet() == 0)
                        complete(null);
                }
            });
        }

        void awaitPending() throws InterruptedException {
            try {
                if (pendingTasks.get() != 0)
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
