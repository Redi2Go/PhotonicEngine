package at.redi2go.photonics.core.rendering.world.compiler;

import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.rendering.WorldOrigin;
import at.redi2go.photonics.core.rendering.world.WorldManager;
import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryMemory;
import at.redi2go.photonics.core.rendering.world.allocator.WorldAllocator;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.tree.BlockMergeMode;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeEntry;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeNode;
import at.redi2go.photonics.core.rendering.world.tree.WorldNode;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RootManager implements WorldManager {
    private final BlockMergeMode mergeMode;
    private final WorldAllocator worldAllocator;

    private final VoxelEntryMemory rootMemory;

    private WorldNode root = null;
    private final Int2ObjectRBTreeMap<List<Runnable>> uploadJobs = new Int2ObjectRBTreeMap<>();

    public RootManager(
            WorldAllocator worldAllocator,
            BlockMergeMode blockMergeMode
    ) {
        this.mergeMode = blockMergeMode;
        this.worldAllocator = worldAllocator;

        this.rootMemory = worldAllocator.allocateEntry(true, 0);
    }

    private WorldNode createNode(int depth, Vector3i treePos) {
        return new WorldNode(this, worldAllocator, mergeMode, depth, treePos);
    }

    public Vector3i minBlockPos() {
        return root == null ? new Vector3i(0) : WorldNode.toBlockPos(root.minTreePos());
    }

    public Vector3i maxBlockPos() {
        return root == null ? new Vector3i(0) : WorldNode.toBlockPos(root.maxTreePos());
    }

    private void initRoot(Vector3i treePos) {
        if (root != null) return;

        root = createNode(VoxelTreeEntry.BLOCK_DEPTH, treePos);
    }

    public void removeRegions(IntSet regions) {
        if (root == null) return;

        root.removeRegions(regions);
    }

    public void insertBlock(Vector3i blockPos, BlockEntry block) {
        blockPos = WorldNode.toTreePos(blockPos);

        initRoot(blockPos);

        if (root.containsBlock(blockPos)) {
            root.insertEntry(blockPos, block);
            return;
        }

        if (isLessThan(blockPos, root.minTreePos())) {
            WorldNode previousRoot = root;

            root = createNode(VoxelTreeEntry.BLOCK_DEPTH, blockPos);
            root.insertEntry(blockPos, block);

            merge(previousRoot.minTreePos(), previousRoot);
        } else merge(blockPos, block);
    }

    private void merge(Vector3i treePos, VoxelTreeEntry entry) {
        while (root.depth() <= entry.depth() || !root.containsBlock(treePos)) {
            WorldNode previousRoot = root;

            root = createNode(previousRoot.depth() + 1, previousRoot.minTreePos());
            root.insertEntry(previousRoot.minTreePos(), previousRoot);
        }

        root.insertEntry(treePos, entry);
    }

    @Override
    public void queueUpload(int depth, Runnable job) {
        uploadJobs.computeIfAbsent(depth, (ignored) -> new ArrayList<>()).add(job);
    }

    public void uploadAll(Supplier<CompilerTask> taskSupplier) throws InterruptedException {
//        var itr = uploadJobs.int2ObjectEntrySet().iterator();
//        while (itr.hasNext()) {
//            var entry = itr.next();
//            var task = taskSupplier.get();
//
//            for (var job : entry.getValue())
//                task.queueJob(job);
//
//            itr.remove();
//            task.awaitCompletion();
//        }

        if (root != null) {
            root.removeEmpty();
            root = root.pruneTree();

            root.uploadTo(rootMemory);
            rootMemory.upload();
        }
    }

    private static boolean isLessThan(Vector3i pos, Vector3i origin) {
        return pos.x < origin.x ||
                pos.y < origin.y ||
                pos.z < origin.z;
    }
}
