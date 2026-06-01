package at.redi2go.photonics.core.rendering.world.compiler;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.joml.Vector3i;

public class RegionIdManager {
    private final Object2IntMap<Vector3i> regions = new Object2IntOpenHashMap<>();
    private final IntList unusedRegions = new IntArrayList();

    private int nextId = 0;

    public RegionIdManager() {
        regions.defaultReturnValue(-1);
    }

    private int getNextId() {
        if (unusedRegions.isEmpty())
            return nextId++;

        return unusedRegions.removeInt(unusedRegions.size() - 1);
    }

    public int getId(Vector3i sectionPos) {
        return regions.computeIfAbsent(sectionPos, (ignored) -> getNextId());
    }

    public void removeRegion(Vector3i sectionPos) {
        var result = regions.removeInt(sectionPos);
        if (result == -1) return;

        unusedRegions.add(result);
    }
}
