package at.redi2go.photonics.core.rendering.world.compiler;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.mc.Minecraft;
import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.ILevel;
import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.rendering.RenderingComponent;
import at.redi2go.photonics.core.rendering.SectionCopy;
import at.redi2go.photonics.core.rendering.SectionManager;
import at.redi2go.photonics.core.rendering.world.BlockRegistry;
import at.redi2go.photonics.core.rendering.world.IgnoredInterruptedException;
import at.redi2go.photonics.core.rendering.world.bakery.BlockBakery;
import at.redi2go.photonics.core.rendering.world.bakery.impl.BlockBakeryImpl;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;
import org.joml.Vector3i;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class ChunkCompiler implements Runnable, RenderingComponent {
    private static final int THREAD_COUNT = 2;

    private final Queue<Vector3i> unloadQueue;
    private final SectionManager.TaskQueue<SectionCopy> sectionQueue;
    private final SectionManager.TaskQueue<BuildResult> builtSectionQueue;

    private final AtlasDownloader atlasDownloader;
    private final BlockRegistry blockRegistry;

    private final ConcurrentMap<Vector3i, Long> sectionHashes = new ConcurrentHashMap<>();
    private final Queue<BlockBakery> bakeryQueue = new ConcurrentLinkedQueue<>();

    private final Thread[] threads = new Thread[THREAD_COUNT];

    public ChunkCompiler(
            SectionManager sectionManager,
            SectionManager.TaskQueue<BuildResult> builtSectionQueue,
            AtlasDownloader atlasDownloader,
            BlockRegistry blockRegistry
    ) {
        this.unloadQueue = sectionManager.newUnloadQueue();
        this.sectionQueue = sectionManager.newSectionQueue();
        this.builtSectionQueue = builtSectionQueue;

        this.atlasDownloader = atlasDownloader;
        this.blockRegistry = blockRegistry;

        for (int i = 0; i < THREAD_COUNT; i++) {
            var thread = new Thread(this, "Photonic Chunk Compiler #" + i);
            threads[i] = thread;

            thread.setDaemon(false);
            thread.start();
        }
    }

    private BlockBakery nextBakery() {
        var bakery = bakeryQueue.poll();
        if (bakery == null)
            bakery = new BlockBakeryImpl(atlasDownloader, blockRegistry);

        bakery.reset();
        return bakery;
    }

    private void releaseBakery(BlockBakery bakery) {
        bakeryQueue.offer(bakery);
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                var section = sectionQueue.take();
                unloadChunks();

                var bakery = nextBakery();

                final long[] hash = {0};

                ILevel level = Minecraft.getLevel();
                if (level == null) {
                    releaseBakery(bakery);
                    continue;
                }

                section.forEachBlock((blockChunkOffset, blockPos, block) -> {
                    hash[0] = hash[0] * 31 + block.hashCode();

                    if (block.isAir()) return;
                    bakery.submitBlock(
                            blockChunkOffset,
                            blockPos,
                            block,
                            level
                    );
                });

                if (Objects.equals(sectionHashes.put(section.pos(), hash[0]), hash[0])) {
                    releaseBakery(bakery);
                    continue;
                }

                builtSectionQueue.offer(section.pos(), new BuildResult(section.pos(), section.blockPos(), bakery));
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

    public class BuildResult implements Disposable {
        private final Vector3i chunkPos;
        private final Vector3i chunkBlockPos;
        private final BlockBakery bakery;

        public BuildResult(Vector3i chunkPos, Vector3i chunkBlockPos, BlockBakery bakery) {
            this.chunkPos = chunkPos;
            this.chunkBlockPos = chunkBlockPos;
            this.bakery = bakery;
        }

        public Vector3i chunkPos() {
            return chunkPos;
        }

        public Vector3i chunkBlockPos() {
            return chunkBlockPos;
        }

        public BlockBakery bakery() {
            return bakery;
        }

        @Override
        public void close() {
            releaseBakery(bakery);
        }
    }
}
