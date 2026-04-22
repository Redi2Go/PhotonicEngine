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

    private final BlockingQueue<Vector3i> queuedSections = new LinkedBlockingQueue<>();

    //TODO: This will need to be a pair of Vector3i and BlockBakery eventually
    private final BlockingQueue<BlockBakery> builtSections = new ArrayBlockingQueue<>(MAX_OUTBOUND_SECTIONS);
    private final Queue<BlockBakery> bakeryQueue = new ConcurrentLinkedQueue<>();

    private final AtlasDownloader atlasDownloader;
    private final BlockRegistry blockRegistry;
    private final WorldCompiler compiler;

    private Thread[] threads = new Thread[THREAD_COUNT];

    public ChunkCompiler(
            AtlasDownloader atlasDownloader,
            BlockRegistry blockRegistry,
            WorldCompiler compiler
    ) {
        this.atlasDownloader = atlasDownloader;
        this.blockRegistry = blockRegistry;
        this.compiler = compiler;

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
                var sectionCoord = queuedSections.take();
                var bakery = nextBakery();

                var level = Minecraft.getLevel();
                if (level == null) {
                    releaseBakery(bakery);
                    continue;
                }

                Vector3i sectionBlockPos = sectionCoord.mul(16, new Vector3i());
                boolean hasNonAir = false;
                var origin = compiler.getOrigin();

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


                            hasNonAir = true;
                            bakery.submitBlock(
                                    origin,
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

                builtSections.put(bakery);
            }
        } catch (InterruptedException e) {

        } catch (Throwable e) {
            Photonics.LOGGER.warn("An exception was throw during chunk compilation!", e);
        }
    }

    public int pendingSections() {
        return queuedSections.size() + builtSections.size();
    }

    public void submitSection(Vector3i section) {
        queuedSections.offer(section);
    }

    public List<BlockBakery> takeSections() throws InterruptedException {
        var result = new ArrayList<BlockBakery>();
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
