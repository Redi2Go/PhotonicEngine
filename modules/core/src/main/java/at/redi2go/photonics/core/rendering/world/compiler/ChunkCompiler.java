package at.redi2go.photonics.core.rendering.world.compiler;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.mc.Minecraft;
import at.redi2go.photonics.api.mc.world.level.ILevel;
import at.redi2go.photonics.api.mc.world.level.chunk.IChunkSection;
import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.RenderingComponent;
import at.redi2go.photonics.core.rendering.SectionCopy;
import at.redi2go.photonics.core.rendering.SectionManager;
import at.redi2go.photonics.core.rendering.world.bakery.BlockBakery;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;
import at.redi2go.photonics.core.rendering.world.block.BlockModel;
import at.redi2go.photonics.core.rendering.world.block.BlockProvider;
import at.redi2go.photonics.core.rendering.world.block.palette.TintBuilder;
import at.redi2go.photonics.core.rendering.world.registry.WorldRegistry;
import at.redi2go.photonics.core.rendering.world.IgnoredInterruptedException;
import org.apache.logging.log4j.util.BiConsumer;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ChunkCompiler implements Runnable, RenderingComponent {
    private static final int THREAD_COUNT = 1;

    private final Queue<Vector3i> unloadQueue;
    private final SectionManager.TaskQueue<SectionCopy> sectionQueue;
    private final SectionManager.TaskQueue<ChunkCompiler.BuildResult> builtSectionQueue;

    private final WorldRegistry worldRegistry;

    private final BlockBakery bakery;

    private final ConcurrentMap<Vector3i, Long> sectionHashes = new ConcurrentHashMap<>();
    private final Thread[] threads = new Thread[THREAD_COUNT];

    public ChunkCompiler(
            SectionManager sectionManager,
            SectionManager.TaskQueue<ChunkCompiler.BuildResult> builtSectionQueue,
            AtlasDownloader atlasDownloader,
            WorldRegistry worldRegistry
    ) {
        this.unloadQueue = sectionManager.newUnloadQueue();
        this.sectionQueue = sectionManager.newSectionQueue();
        this.builtSectionQueue = builtSectionQueue;

        this.worldRegistry = worldRegistry;

        this.bakery = BlockBakery.newBakery(atlasDownloader);

        for (int i = 0; i < THREAD_COUNT; i++) {
            var thread = new Thread(this, "Photonic Chunk Compiler #" + i);
            threads[i] = thread;

            thread.setDaemon(false);
            thread.start();
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                var section = sectionQueue.take();
                unloadChunks();

                ILevel level = Minecraft.getLevel();
                if (level == null) continue;

                // Computing the hash immediately is cheaper than meshing an entire section just to discard it
                long hash = section.computeSectionHash();
                if (Objects.equals(sectionHashes.get(section.pos()), hash)) continue;

                var buildResult = new BuildResult(section.pos(), section.blockPos(), hash);

                section.forEachBlock((blockChunkOffset, blockPos, block) -> {
                    if (block.isAir()) return;

                    var meshResult = bakery.meshBlock(
                            new Vector3i(blockChunkOffset),
                            blockPos,
                            block,
                            level
                    );

                    if (meshResult == null) return;

                    buildResult.submitBlockFuture(
                            blockChunkOffset.x(),
                            blockChunkOffset.y(),
                            blockChunkOffset.z(),
                            meshResult.tintData(),
                            worldRegistry.createBlockModel(meshResult)
                    );
                });

                buildResult.awaitSubmission();
            }
        } catch (InterruptedException | IgnoredInterruptedException e) {

        } catch (Throwable e) {
            Photonics.LOGGER.warn("An exception was throw during chunk compilation!", e);
        }
    }

    private void unloadChunks() {
        while (!unloadQueue.isEmpty()) {
            var section = unloadQueue.poll();
            if (section == null) continue;

            sectionHashes.remove(section);
        }
    }

    @Override
    public void close() {
        for (var thread : threads)
            thread.interrupt();
    }

    public class BuildResult {
        private final Vector3i chunkPos;
        private final Vector3i chunkBlockPos;
        private final long hash;
        private final @Nullable BlockModel[] blocks = new BlockModel[IChunkSection.SECTION_SIZE];

        private final AtomicInteger pendingBlocks = new AtomicInteger();
        private final CompletableFuture<Void> future = new CompletableFuture<>();

        public BuildResult(Vector3i chunkPos, Vector3i chunkBlockPos, long hash) {
            this.chunkPos = chunkPos;
            this.chunkBlockPos = chunkBlockPos;
            this.hash = hash;
        }

        public Vector3i chunkPos() {
            return chunkPos;
        }

        public Vector3i chunkBlockPos() {
            return chunkBlockPos;
        }

        private void submitBlockFuture(
                int x, int y, int z,
                TintBuilder.Result tintInfo,
                CompletionStage<BlockProvider> block
        ) {
            while (true) {
                int pending = pendingBlocks.get();
                if (pending == -1) throw new IllegalStateException();

                int remaining = (pending & Integer.MAX_VALUE) + 1;
                if (pendingBlocks.compareAndSet(pending, remaining | (pending & Integer.MIN_VALUE))) {
                    block.thenAccept((result) -> {
                        try {
                            completeBlock(x, y, z, result.createVariant(tintInfo));
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (Throwable t) {
                            Photonics.LOGGER.error("Error while setting block", t);
                        }
                    });

                    return;
                }
            }
        }

        private void completeBlock(int x, int y, int z, BlockModel blockModel) throws InterruptedException {
            setBlock(x, y, z, blockModel);

            while (true) {
                int pending = pendingBlocks.get();
                if (pending == -1) throw new IllegalStateException();

                int remaining = (pending & Integer.MAX_VALUE) - 1;
                if (pendingBlocks.compareAndSet(pending, remaining | (pending & Integer.MIN_VALUE))) {
                    trySubmit();

                    return;
                }
            }
        }

        private void trySubmit() throws InterruptedException {
            while (true) {
                int pending = pendingBlocks.get();
                if (pending == -1 || (pending & Integer.MIN_VALUE) == 0) return;

                int remaining = pending & Integer.MAX_VALUE;
                if (remaining > 0) return;

                // This is safe because the max possible pending is 4096
                if (pendingBlocks.compareAndSet(pending, -1)) {
                    future.complete(null);

                    return;
                }
            }
        }

        private void awaitSubmission() throws InterruptedException, ExecutionException {
            while (true) {
                int pending = pendingBlocks.get();
                if ((pending & Integer.MIN_VALUE) != 0) return;

                if (pendingBlocks.compareAndSet(pending, pending | Integer.MIN_VALUE)) {
                    trySubmit();
                    break;
                }
            }


            future.get();
            var previousHash = sectionHashes.put(chunkPos(), hash);

            if (!Objects.equals(previousHash, hash))
                builtSectionQueue.offer(chunkPos, this);
        }

        public @Nullable BlockModel getBlock(int x, int y, int z) {
            if (!containsBlock(x, y, z)) return null;

            return blocks[VoxelModel.toVoxelIndex(x, y, z)];
        }

        private void setBlock(int x, int y, int z, @Nullable BlockModel block) {
            if (!containsBlock(x, y, z)) return;

            blocks[VoxelModel.toVoxelIndex(x, y, z)] = block;
        }

        public void forEachBlock(BiConsumer<Vector3i, BlockModel> blockConsumer) {
            Vector3i chunkOffset = new Vector3i();

            for (int px = 0; px < 16; px++) {
                for (int py = 0; py < 16; py++) {
                    for (int pz = 0; pz < 16; pz++) {

                        var block = getBlock(px, py, pz);
                        if (block == null) continue;

                        chunkOffset.set(px, py, pz);
                        blockConsumer.accept(chunkOffset, block);
                    }
                }
            }
        }

        public void discard() {
            for (int i = 0; i < IChunkSection.SECTION_SIZE; i++) {
                var block = blocks[i];
                if (block == null) continue;

                block.parts().forEach(Disposable::close);
            }
        }

        private static boolean containsBlock(int x, int y, int z) {
            return VoxelModel.contains(x, y, z, 16, 16, 16);
        }
    }
}
