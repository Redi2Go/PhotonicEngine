package at.redi2go.photonics.core.rendering.world.compiler;

import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.rendering.world.WorldManager;
import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryMemory;
import at.redi2go.photonics.core.rendering.world.allocator.WorldAllocator;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.tree.BlockMergeMode;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeEntry;
import at.redi2go.photonics.core.rendering.world.tree.nodes.ChunkNode;
import at.redi2go.photonics.core.rendering.world.tree.nodes.WorldNode;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.joml.Vector3i;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public class TreeManager implements WorldManager {
    private final BlockMergeMode mergeMode;
    private final WorldAllocator worldAllocator;

    private final Set<ChunkNode> chunks;
    private final List<Queue<Runnable>> uploadQueue;

    private WorldNode root;
    private final VoxelEntryMemory rootMemory;

    public TreeManager(BlockMergeMode mergeMode, WorldAllocator worldAllocator) {
        this.mergeMode = mergeMode;
        this.worldAllocator = worldAllocator;

        this.chunks = ConcurrentHashMap.newKeySet();
        var listBuilder = ImmutableList.<Queue<Runnable>>builder();

        for (int i = 0; i < 11; i++)
            listBuilder.add(new ConcurrentLinkedQueue<>());

        this.uploadQueue = listBuilder.build();
        this.rootMemory = worldAllocator.allocateEntry(true, 0);
    }

    public Vector3i minBounds() {
        var root = this.root;
        return root == null ? new Vector3i(0) : root.minBounds();
    }

    public Vector3i maxBounds() {
        var root = this.root;
        return root == null ? new Vector3i(0) : root.maxBounds();
    }

    @Override
    public void addChunk(ChunkNode chunk) {
        chunks.add(chunk);
    }

    @Override
    public void removeChunk(ChunkNode chunk) {
        chunks.remove(chunk);
    }

    @Override
    public void queueUpload(int depth, Runnable job) {
        uploadQueue.get(depth).add(job);
    }

    private WorldNode createNode(int depth, Vector3i pos) {
        return WorldNode.create(pos.x, pos.y, pos.z, this, worldAllocator, mergeMode, depth);
    }

    private void initRoot(Vector3i pos) {
        if (root != null) return;

        root = createNode(VoxelTreeEntry.BLOCK_CONTAINER_DEPTH, pos);
    }

    public void insertBlock(Vector3i pos, BlockEntry entry) {
        initRoot(pos);

        if (root.isInBounds(pos)) {
            root.insertEntry(pos, entry);
            return;
        }

        if (isLessThan(pos, root.minBounds())) {
            WorldNode previousRoot = root;

            root = createNode(VoxelTreeEntry.BLOCK_CONTAINER_DEPTH, pos);
            root.insertEntry(pos, entry);

            merge(previousRoot.minBounds(), previousRoot);
        } else merge(pos, entry);
    }

    private void insertChunk(ChunkNode node) {
        if (root == null) {
            root = node;
            return;
        }

        if (isLessThan(node.minBounds(), root.minBounds())) {
            WorldNode previousRoot = root;

            root = node;
            merge(previousRoot.minBounds(), previousRoot);
        } else merge(node.minBounds(), node);
    }

    private void merge(Vector3i pos, VoxelTreeEntry entry) {
        while (root.depth() <= entry.depth() || !root.isInBounds(pos)) {
            WorldNode previousRoot = root;

            root = createNode(previousRoot.depth() + 1, previousRoot.minBounds());
            root.insertEntry(previousRoot.minBounds(), previousRoot);
        }

        root.insertEntry(pos, entry);
    }

    public void removeRegions(IntSet regions) {
        if (root == null) return;

        var ignored = root.removeRegions(regions);
    }

    public void recenter(Vector3i offset) {
        for (var chunk : chunks)
            chunk.removeFromTree();

        root.close();
        root = null;

        for (var chunk : chunks) {
            chunk.recenter(offset);
            insertChunk(chunk);
        }
    }

    public void uploadAll(Supplier<CompilerTask> taskSupplier) throws InterruptedException {
        root = root.removeEmpty();

        for (Queue<Runnable> queue : uploadQueue) {
            var task = taskSupplier.get();

            while (!queue.isEmpty()) {
                var job = queue.poll();
                if (job == null) break;

                task.queueJob(job);
            }

            task.awaitCompletion();
        }

        if (root == null) return;

        root = root.trim();
        if (root == null) {
            rootMemory.setChildMask(0);
            rootMemory.upload();

            return;
        }

        root.uploadTo(rootMemory);
        rootMemory.upload();
    }

    public void findBounds(Vector3i minBlock, Vector3i maxBlock) {
        minBlock.set(Integer.MAX_VALUE);
        maxBlock.set(Integer.MIN_VALUE);

        for (var chunk : chunks) {
            minBlock.min(chunk.minBounds());
            maxBlock.max(chunk.maxBounds());
        }
    }

    private static boolean isLessThan(Vector3i pos, Vector3i origin) {
        return pos.x < origin.x ||
                pos.y < origin.y ||
                pos.z < origin.z;
    }
}
