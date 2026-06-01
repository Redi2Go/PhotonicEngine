package at.redi2go.photonics.core.rendering.world.compiler;

import at.redi2go.photonics.api.mc.Minecraft;
import at.redi2go.photonics.core.iris.pipeline.uniform.IDynamicUniformHolder;
import at.redi2go.photonics.core.iris.pipeline.uniform.IUniformUpdateFrequency;
import at.redi2go.photonics.core.rendering.RenderingComponent;
import at.redi2go.photonics.core.rendering.SectionManager;
import at.redi2go.photonics.core.rendering.UniformUpdater;
import at.redi2go.photonics.core.rendering.WorldOrigin;
import at.redi2go.photonics.core.rendering.world.IgnoredInterruptedException;
import at.redi2go.photonics.core.rendering.world.WorldManager;
import at.redi2go.photonics.core.rendering.world.allocator.WorldAllocator;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTexture;
import at.redi2go.photonics.core.rendering.world.registry.WorldRegistry;
import at.redi2go.photonics.core.rendering.world.tree.BlockMergeMode;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class WorldCompiler implements Runnable, RenderingComponent {
    public static final int MAX_SECTIONS_PER_RUN = 48;

    private static final int THREAD_POOL_SIZE = 3;
    private static final ExecutorService THREAD_POOL;

    private final SectionManager.TaskQueue<ChunkCompiler.BuildResult> taskQueue;

    private final WorldAllocator worldAllocator;
    private final PaletteTexture paletteTexture;

    private final WorldRegistry registry;

    private final RegionIdManager regionIds = new RegionIdManager();
    private final RootManager rootManager;

    private final ReentrantLock uploadLock = new ReentrantLock();
    private final Condition uploadDone = uploadLock.newCondition();
    private boolean canUpload = true;

    private final UniformUpdater uniformUpdater = new UniformUpdater();

    private WorldOrigin mostRecentOrigin = new WorldOrigin(0.0f, 0.0f, 0.0f);
    private Vector3f mostRecentMinBlock = new Vector3f();
    private Vector3f mostRecentMaxBlock = new Vector3f();

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

        this.rootManager = new RootManager(worldAllocator, BlockMergeMode.OVERWRITE);

        this.compilerThread = new Thread(this, "Photonics World Compiler");
        this.compilerThread.start();
    }

    public WorldOrigin origin() {
        return mostRecentOrigin;
    }

    public Vector3f minBlock() {
        return mostRecentMinBlock;
    }

    public Vector3f maxBlock() {
        return mostRecentMaxBlock;
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
                    clearPendingSections(builtSections);
                    insertSections(builtSections);
                }

                if (!unloadedSections.isEmpty() || !builtSections.isEmpty()) {
                    stopUpload();
                    rootManager.uploadAll(MultiThreadTask::new);
                    awaitUpload();

                    registry.freeUnusedObjects();
                }
            }
        } catch (InterruptedException | IgnoredInterruptedException e) {

        }
    }

    private void clearUnloadedSections(List<Vector3i> unloadedSections) {
        IntSet regions = new IntOpenHashSet(unloadedSections.size());
        for (var section : unloadedSections) {
            regions.add(regionIds.getId(section));
            regionIds.removeRegion(section);
        }

        rootManager.removeRegions(regions);
    }

    private void clearPendingSections(List<ChunkCompiler.BuildResult> sections) {
        IntSet regions = new IntOpenHashSet(sections.size());
        for (var section : sections) {
            regions.add(regionIds.getId(section.chunkPos()));
        }

        rootManager.removeRegions(regions);
    }

    private void insertSections(List<ChunkCompiler.BuildResult> sections) {
        BlockSorter blockSorter = new BlockSorter();
        Vector3i blockPos = new Vector3i();

        for (var section : sections) {
            try (section) {
                blockSorter.reset();

                int region = regionIds.getId(section.chunkPos());

                section.forEachBlock(((blockChunkOffset, block) ->
                        blockSorter.addBlock(
                                section.chunkBlockPos().add(blockChunkOffset, blockPos),
                                block
                        )
                ));

                blockSorter.forEachBlock((block) -> {
                    var parts = block.blockModel().parts();
                    for (int i = 0; i < parts.size(); i++) {
                        var part = parts.get(i);

                        blockPos.set(block.x(), block.y(), block.z());
                        blockPos.add(part.offset());

                        rootManager.insertBlock(
                                blockPos,
                                part.createEntry(region)
                        );
                    }
                });
            }
        }
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

            mostRecentOrigin = new WorldOrigin(rootManager.minBlockPos());

            mostRecentMinBlock = new Vector3f(rootManager.minBlockPos());
            mostRecentMaxBlock = new Vector3f(rootManager.maxBlockPos());

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

        dynamicUniforms.uniform3f("world_min_block", () -> new Vector3f(mostRecentOrigin.applyOffset(mostRecentMinBlock)), uniformUpdater.newNotifier());
        dynamicUniforms.uniform3f("world_max_block", () -> new Vector3f(mostRecentOrigin.applyOffset(mostRecentMaxBlock)), uniformUpdater.newNotifier());
        dynamicUniforms.uniform3f("world_tree_size", () -> new Vector3f(mostRecentMaxBlock).sub(mostRecentMinBlock), uniformUpdater.newNotifier());

        dynamicUniforms.uniform3d(
                IUniformUpdateFrequency.perFrame(),
                "rt_camera_position",
                () -> mostRecentOrigin.applyOffset(Minecraft.getCameraPos())
        );
    }

    @Override
    public void close() {
        compilerThread.interrupt();
    }

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

        @Override
        public void awaitCompletion() throws InterruptedException {
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

        @Override
        public void awaitCompletion() {

        }
    }

    static {
        AtomicInteger count = new AtomicInteger(0);
        THREAD_POOL = Executors.newFixedThreadPool(THREAD_POOL_SIZE, (r) ->
                new Thread(r, "Photonics World Worker #" + count.getAndIncrement()));
    }
}
