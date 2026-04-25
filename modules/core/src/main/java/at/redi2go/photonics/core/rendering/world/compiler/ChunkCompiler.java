package at.redi2go.photonics.core.rendering.world.compiler;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.mc.Minecraft;
import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.rendering.world.BlockRegistry;
import at.redi2go.photonics.core.rendering.world.bakery.BlockBakery;
import at.redi2go.photonics.core.rendering.world.bakery.Vertex;
import at.redi2go.photonics.core.rendering.world.bakery.impl.BlockBakeryImpl;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;
import it.unimi.dsi.fastutil.Pair;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class ChunkCompiler implements Runnable, Disposable {
    private static final int THREAD_COUNT = 2;
    private static final int MAX_OUTBOUND_SECTIONS = 24;

    private final SectionQueue sectionQueue;

    private final ConcurrentMap<Vector3i, Long> sectionHash = new ConcurrentHashMap<>();
    private final BlockingQueue<BuildResult> builtSections = new ArrayBlockingQueue<>(MAX_OUTBOUND_SECTIONS);
    private final Queue<BlockBakery> bakeryQueue = new ConcurrentLinkedQueue<>();

    private final AtlasDownloader atlasDownloader;
    private final BlockRegistry blockRegistry;

    private Thread[] threads = new Thread[THREAD_COUNT];

    public ChunkCompiler(
            AtlasDownloader atlasDownloader,
            BlockRegistry blockRegistry,
            SectionQueue sectionQueue
    ) {
        this.atlasDownloader = atlasDownloader;
        this.blockRegistry = blockRegistry;
        this.sectionQueue = sectionQueue;

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
                var sectionCoord = sectionQueue.takeSection();
                var bakery = nextBakery();

                var level = Minecraft.getLevel();
                if (level == null) {
                    releaseBakery(bakery);
                    continue;
                }

                Vector3i sectionBlockPos = sectionCoord.mul(16, new Vector3i());
                Vector3i blockChunkOffset = new Vector3i();
                boolean hasNonAir = false;

                long hash = 0;

                for (int px = 0; px < 16; px++) {
                    for (int py = 0; py < 16; py++) {
                        for (int pz = 0; pz < 16; pz++) {
                            var blockPos = IBlockPos.of(
                                    sectionBlockPos.x + px,
                                    sectionBlockPos.y + py,
                                    sectionBlockPos.z + pz
                            );

                            var block = level.getBlockState(blockPos);
                            hash = hash * 31 + block.hashCode();

                            if (block.isAir()) continue;

                            blockChunkOffset.set(px, py, pz);

                            hasNonAir = true;
                            bakery.submitBlock(
                                    blockChunkOffset,
                                    blockPos,
                                    block,
                                    level
                            );
                        }
                    }
                }

                if (!hasNonAir || Objects.equals(sectionHash.put(sectionCoord, hash), hash)) {
                    releaseBakery(bakery);
                    continue;
                }

                builtSections.put(new BuildResult(new Vector3i(sectionCoord), sectionBlockPos, bakery));
            }
        } catch (InterruptedException e) {

        } catch (Throwable e) {
            Photonics.LOGGER.warn("An exception was throw during chunk compilation!", e);
        }
    }

    public List<BuildResult> takeSections() throws InterruptedException {
        var result = new ArrayList<BuildResult>();
        result.add(builtSections.take());
        builtSections.drainTo(result);

        return result;
    }

    public void unloadSection(Vector3i section) {
        sectionHash.remove(section);
    }

    @Override
    public void close() {
        for (var thread : threads)
            thread.interrupt();
    }

    public class BuildResult implements Disposable{
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
