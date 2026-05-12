package at.redi2go.photonics.core.rendering.world.compiler;

import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.mc.Minecraft;
import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.iris.pipeline.buffer.IBufferHolder;
import at.redi2go.photonics.core.iris.pipeline.uniform.IDynamicUniformHolder;
import at.redi2go.photonics.core.rendering.RenderingComponent;
import at.redi2go.photonics.core.rendering.SectionManager;
import at.redi2go.photonics.core.rendering.ThreadRunnable;
import at.redi2go.photonics.core.rendering.UniformUpdater;
import at.redi2go.photonics.core.rendering.world.BlockRegistry;
import at.redi2go.photonics.core.rendering.world.IgnoredInterruptedException;
import at.redi2go.photonics.core.rendering.world.WorldOrigin;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTexture;
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

public class WorldCompiler implements Runnable, RenderingComponent {
    public static final int MAX_SECTIONS_PER_RUN = 12;

    private static final int THREAD_POOL_SIZE = 3;
    private static final ExecutorService THREAD_POOL;

    private final Queue<Vector3i> unloadQueue;
    private final SectionManager.TaskQueue<ChunkCompiler.BuildResult> builtSectionQueue;

    private final IGpuBufferHeap heap;
    private final PaletteTexture paletteTexture;
    private final BlockRegistry registry;

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
            SectionManager sectionManager,
            SectionManager.TaskQueue<ChunkCompiler.BuildResult> builtSectionQueue,
            IGpuBufferHeap heap,
            PaletteTexture paletteTexture,
            BlockRegistry blockRegistry
    ) {
        this.unloadQueue = sectionManager.newUnloadQueue();
        this.builtSectionQueue = builtSectionQueue;
        this.heap = heap;
        this.paletteTexture = paletteTexture;
        this.registry = blockRegistry;

        this.uploadQueue = new ConcurrentLinkedQueue<>();
        this.rootVoxel = WorldVoxel.create(depth, this, registry, heap, uploadQueue);

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
                unloadSections();
                rootVoxel.pruneEmptyVoxels();

                registry.freeUnusedBlocks();

                var sections = builtSectionQueue.drain(MAX_SECTIONS_PER_RUN);
                recenter();

                clearPendingSections(sections);

                voxelizeSections(sections);

                stopUpload();
                buildSections();
                awaitUpload();
            }
        } catch (InterruptedException | IgnoredInterruptedException e) {

        }
    }

    // Compiler Steps

    private void unloadSections() {
        if (iorigin == null) return;

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
        var newOrigin = WorldOrigin.getAsVector3i();
        if (iorigin == null) {
            setOrigin(newOrigin);
            return;
        }

        if (iorigin.equals(newOrigin)) return;

        stopUpload();

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

        var chunks = new ArrayList<>(this.chunks);

        while (!uploadQueue.isEmpty())
            task.queueJob(uploadQueue.remove()::upload);

        minVoxel.set(Integer.MAX_VALUE);
        maxVoxel.set(Integer.MIN_VALUE);

        var temp = new Vector3i();

        for (var chunk : chunks) {
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

            heap.upload();
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

    @Override
    public void registerBuffers(IBufferHolder buffers) {
        buffers.addDefaultBufferHeap("ph_world_voxel_buffer", () -> heap);
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
