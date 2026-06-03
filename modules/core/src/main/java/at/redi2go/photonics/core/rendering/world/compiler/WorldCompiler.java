package at.redi2go.photonics.core.rendering.world.compiler;

import at.redi2go.photonics.api.mc.Minecraft;
import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.iris.pipeline.uniform.IDynamicUniformHolder;
import at.redi2go.photonics.core.iris.pipeline.uniform.IUniformUpdateFrequency;
import at.redi2go.photonics.core.rendering.RenderingComponent;
import at.redi2go.photonics.core.rendering.SectionManager;
import at.redi2go.photonics.core.rendering.UniformUpdater;
import at.redi2go.photonics.core.rendering.WorldOrigin;
import at.redi2go.photonics.core.rendering.world.IgnoredInterruptedException;
import at.redi2go.photonics.core.rendering.world.allocator.WorldAllocator;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTexture;
import at.redi2go.photonics.core.rendering.world.registry.WorldRegistry;
import at.redi2go.photonics.core.rendering.world.tree.BlockMergeMode;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeEntry;
import at.redi2go.photonics.core.rendering.world.tree.entries.LightBlockEntry;
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

//TODO: FIX TREE GROWING TO GIANT SIZE WHEN TELEPORTED
public class WorldCompiler implements Runnable, RenderingComponent {
    public static final int MAX_SECTIONS_PER_RUN = 48;

    private static final int THREAD_POOL_SIZE = 3;
    private static final ExecutorService THREAD_POOL;

    private final SectionManager.TaskQueue<ChunkCompiler.BuildResult> taskQueue;

    private final WorldAllocator worldAllocator;
    private final PaletteTexture paletteTexture;

    private final WorldRegistry registry;

    private final RegionIdManager regionIds = new RegionIdManager();
    private final TreeManager treeManager;

    private final ReentrantLock uploadLock = new ReentrantLock();
    private final Condition uploadDone = uploadLock.newCondition();
    private boolean canUpload = true;

    private Vector3i iorigin = null;
    private WorldOrigin offset = null;

    private final Vector3i minBlock = new Vector3i();
    private final Vector3i maxBlock = new Vector3i();

    private final UniformUpdater uniformUpdater = new UniformUpdater();

    private WorldOrigin mostRecentOrigin = new WorldOrigin(0.0f, 0.0f, 0.0f);

    private Vector3f mostRecentMinBounds = new Vector3f();
    private Vector3f mostRecentMaxBounds = new Vector3f();


    private Vector3f mostRecentMinBlock = new Vector3f();
    private Vector3f mostRecentMaxBlock = new Vector3f();

    private int mostRecentBlockContainerScale = 0;

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

        this.treeManager = new TreeManager(BlockMergeMode.OVERWRITE, worldAllocator);

        this.compilerThread = new Thread(this, "Photonics World Compiler");
        this.compilerThread.start();
    }

    public WorldOrigin origin() {
        return mostRecentOrigin;
    }

    private void setOrigin(Vector3i origin) {
        this.iorigin = origin;
        this.offset = new WorldOrigin(origin.x, origin.y, origin.z);
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                taskQueue.awaitTask();

                var unloadedSections = taskQueue.drainUnloadQueue();
                if (!unloadedSections.isEmpty())
                    clearUnloadedSections(unloadedSections);


                var builtSections = taskQueue.drain(MAX_SECTIONS_PER_RUN);
                if (!builtSections.isEmpty()) {
                    recenter();

                    clearPendingSections(builtSections);
                    insertSections(builtSections);
                }

                if (!unloadedSections.isEmpty() || !builtSections.isEmpty()) {
                    stopUpload();
                    writeSections();
                    awaitUpload();

                    registry.freeUnusedObjects();
                }
            }
        } catch (Throwable t) {
            if (t instanceof InterruptedException) return;
            if (IgnoredInterruptedException.shouldIgnore(t)) return;

            Photonics.LOGGER.error("An error was thrown during world compilation!", t);
        }
    }


    // Compiler steps

    private void clearUnloadedSections(List<Vector3i> unloadedSections) {
        if (iorigin == null) return;

        IntSet regions = new IntOpenHashSet(unloadedSections.size());
        for (var section : unloadedSections) {
            regions.add(regionIds.getId(section));
            regionIds.removeRegion(section);
        }

        treeManager.removeRegions(regions);
    }

    private void recenter() throws InterruptedException {
        var newOrigin = WorldOrigin.getAsVector3i();
        if (iorigin == null) {
            setOrigin(newOrigin);
            return;
        }

        if (iorigin.equals(newOrigin)) return;

        stopUpload();

        var offset = iorigin.sub(newOrigin, new Vector3i());
        treeManager.recenter(offset);

        setOrigin(newOrigin);
    }

    private void clearPendingSections(List<ChunkCompiler.BuildResult> sections) {
        IntSet regions = new IntOpenHashSet(sections.size());
        for (var section : sections)
            regions.add(regionIds.getId(section.chunkPos()));

        treeManager.removeRegions(regions);
    }

    private void insertSections(List<ChunkCompiler.BuildResult> sections) {
        BlockSorter blockSorter = new BlockSorter();
        Vector3i blockPos = new Vector3i();

        for (var section : sections) {
            try (section) {
                blockSorter.reset();

                var chunkBlockPos = new Vector3i(section.chunkBlockPos())
                        .sub(iorigin);

                int region = regionIds.getId(section.chunkPos());
                section.forEachBlock((blockChunkOffset, blockState, blockModel) -> blockSorter.addBlock(
                        chunkBlockPos.add(blockChunkOffset, new Vector3i()),
                        blockState,
                        blockModel
                ));

                blockSorter.forEachBlock((block) -> {
                    var parts = block.blockModel().parts();
                    if (parts.isEmpty()) return;

                    var light = registry.lightRegistry().getWeak(block.blockState());

                    for (int i = 0; i < parts.size(); i++) {
                        var part = parts.get(i);

                        blockPos.set(block.x(), block.y(), block.z());
                        blockPos.add(part.offset());

                        var entry = part.createEntry(region);
                        if (light != null) {
                            light.acquireReference();
                            entry = new LightBlockEntry(entry, light);
                        }

                        treeManager.insertBlock(
                                blockPos,
                                entry
                        );
                    }
                });
            }
        }
    }

    private void writeSections() throws InterruptedException {
        treeManager.uploadAll(MultiThreadTask::new);
        treeManager.findBounds(minBlock, maxBlock);
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

            mostRecentMinBounds = new Vector3f(treeManager.minBounds());
            mostRecentMaxBounds = new Vector3f(treeManager.maxBounds());

            mostRecentOrigin = offset == null ? new WorldOrigin(0, 0, 0) : offset;

            mostRecentMinBlock = new Vector3f(minBlock);
            mostRecentMaxBlock = new Vector3f(maxBlock);

            mostRecentBlockContainerScale = 21 - (treeManager.depth() - (VoxelTreeEntry.BLOCK_CONTAINER_DEPTH) << 1);

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

        dynamicUniforms.uniform3f("world_min_block", () -> new Vector3f(mostRecentMinBlock), uniformUpdater.newNotifier());
        dynamicUniforms.uniform3f("world_max_block", () -> new Vector3f(mostRecentMaxBlock), uniformUpdater.newNotifier());

        dynamicUniforms.uniform3f("world_tree_size", () -> new Vector3f(mostRecentMaxBounds).sub(mostRecentMinBounds), uniformUpdater.newNotifier());
        dynamicUniforms.uniform1i("world_block_scale_exp", () -> mostRecentBlockContainerScale, uniformUpdater.newNotifier());

        dynamicUniforms.uniform3f(
                IUniformUpdateFrequency.perFrame(),
                "rt_camera_position",
                () -> {
                    var offset = mostRecentOrigin;
                    if (offset == null) return new Vector3f(0f);

                    var pos = Minecraft.getCameraPos();
                    return new Vector3f(offset.applyOffset(new Vector3d(pos.x, pos.y, pos.z)));
                }
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
