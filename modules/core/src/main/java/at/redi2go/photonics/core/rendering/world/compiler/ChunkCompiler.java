package at.redi2go.photonics.core.rendering.world.compiler;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.mc.Minecraft;
import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.rendering.world.BlockRegistry;
import at.redi2go.photonics.core.rendering.world.WorldOrigin;
import at.redi2go.photonics.core.rendering.world.bakery.BlockBakery;
import at.redi2go.photonics.core.rendering.world.bakery.impl.BlockBakeryImpl;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class ChunkCompiler implements Runnable, Disposable {
    private static final int THREAD_COUNT = 2;
    private static final int MAX_OUTBOUND_SECTIONS = 24;

    private final SectionQueue sectionQueue;

    private final BlockingQueue<Pair<Vector3i, BlockBakery>> builtSections = new ArrayBlockingQueue<>(MAX_OUTBOUND_SECTIONS);
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

    public void releaseBakery(BlockBakery bakery) {
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

                for (int px = 0; px < 16; px++) {
                    for (int py = 0; py < 16; py++) {
                        for (int pz = 0; pz < 16; pz++) {
                            var blockPos = IBlockPos.of(
                                    sectionBlockPos.x + px,
                                    sectionBlockPos.y + py,
                                    sectionBlockPos.z + pz
                            );

                            var block = level.getBlockState(blockPos);
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

                if (!hasNonAir) {
                    releaseBakery(bakery);
                    continue;
                }

                builtSections.put(Pair.of(sectionBlockPos, bakery));
            }
        } catch (InterruptedException e) {

        } catch (Throwable e) {
            Photonics.LOGGER.warn("An exception was throw during chunk compilation!", e);
        }
    }

    public List<Pair<Vector3i, BlockBakery>> takeSections() throws InterruptedException {
        var result = new ArrayList<Pair<Vector3i, BlockBakery>>();
        result.add(builtSections.take());
        builtSections.drainTo(result);

        return result;
    }

    @Override
    public void close() {
        for (var thread : threads)
            thread.interrupt();
    }
//    private final ReentrantLock lock = new ReentrantLock();
//
//    private List<>
//
//    private class QueuedSection extends Vector3i implements Comparable<QueuedSection> {
//        private float distance = Integer.MAX_VALUE;
//        private int distMod = mod - 1;
//
//        private float getDistance() {
//            if (mod == distMod) return distance;
//
//            return (float) distance(cameraChunkPos);
//        }
//
//        @Override
//        public int compareTo(@NotNull ChunkCompiler.QueuedSection o) {
//            return Float.compare(o.getDistance(), getDistance());
//        }
//    }
}
