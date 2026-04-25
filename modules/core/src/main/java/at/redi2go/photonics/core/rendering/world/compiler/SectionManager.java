package at.redi2go.photonics.core.rendering.world.compiler;

import at.redi2go.photonics.api.mc.Minecraft;
import at.redi2go.photonics.api.mc.world.level.ILevel;
import at.redi2go.photonics.api.mc.world.level.chunk.IChunkSection;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.NonNls;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntSupplier;

public class SectionManager {
    private final Set<Vector3i> loadedSections = ConcurrentHashMap.newKeySet();

    private final Queue<Vector3i> unloadSections = new ConcurrentLinkedQueue<>();

    private final TaskQueue<SectionCopy> sectionMeshQueue = new TaskQueue<>(-1);
    private final TaskQueue<ChunkCompiler.BuildResult> buildSections = new TaskQueue<>(WorldCompiler.MAX_SECTIONS_PER_RUN << 1);

    private final IntSupplier renderDistanceSupplier;

    public SectionManager(IntSupplier renderDistanceSupplier) {
        this.renderDistanceSupplier = renderDistanceSupplier;
    }

    private Vector3i lastCameraPos = null;
    private int lastRenderDistance = -1;

    private void refreshSections(@NonNls ILevel level) throws InterruptedException {
        var cameraPos = getCameraChunkPos();
        int rd = renderDistanceSupplier.getAsInt();

        if (rd == lastRenderDistance && Objects.equals(lastCameraPos, cameraPos)) return;

        lastCameraPos = cameraPos;
        lastRenderDistance = rd;


        Set<Vector3i> discoveredSections = new HashSet<>();
        Set<Vector3i> unloadedSections = new HashSet<>();
        List<Pair<Vector3i, SectionCopy>> sectionsToMesh = new ArrayList<>();

        rd+= 2;

        for (int px = -rd; px <= rd; px++) {
            for (int pz = -rd; pz <= rd; pz++) {
                int sectionX = cameraPos.x + px;
                int sectionZ = cameraPos.z + pz;

                var chunk = level.getChunkOrNull(sectionX, sectionZ);
                if (chunk == null) continue;

                IChunkSection[] sections = chunk.sections();

                for (int py = -rd; py <= rd; py++) {
                    int sectionY = cameraPos.y + py;
                    int sectionIndex = level.getSectionIndexFromSectionY(sectionY);
                    if (sectionIndex < 0) continue;
                    if (sectionIndex >= sections.length) break;

                    var section = sections[sectionIndex];
                    if (section.hasOnlyAir()) continue;

                    var sectionCoord = new Vector3i(sectionX, sectionY, sectionZ);

                    discoveredSections.add(sectionCoord);

                    if (!loadedSections.contains(sectionCoord))
                        sectionsToMesh.add(Pair.of(sectionCoord, new SectionCopy(sectionCoord, section)));
                }
            }
        }

        for (var itr = loadedSections.iterator(); itr.hasNext(); ) {
            var section = itr.next();
            if (discoveredSections.contains(section)) continue;

            itr.remove();
            unloadedSections.add(section);
        }

        loadedSections.addAll(discoveredSections);
        unloadSections.addAll(unloadedSections);

        sectionMeshQueue.offerMany(sectionsToMesh);
    }

    public Queue<Vector3i> unloadedSections() {
        return unloadSections;
    }

    public TaskQueue<ChunkCompiler.BuildResult> builtSections() {
        return buildSections;
    }

    public TaskQueue<SectionCopy> sectionMeshQueue() {
        return sectionMeshQueue;
    }

    private Optional<SectionCopy> createCopy(Vector3i sectionPos, ILevel level) {
        var chunk = level.getChunkOrNull(sectionPos.x, sectionPos.z);
        if (chunk == null) return Optional.empty();

        var sections = chunk.sections();

        var sectionIndex = level.getSectionIndexFromSectionY(sectionPos.y);
        if (sectionIndex < 0) return Optional.empty();
        if (sectionIndex >= sections.length) return Optional.empty();

        var section = sections[sectionIndex];
        if (section.hasOnlyAir()) return Optional.empty();

        return Optional.of(new SectionCopy(sectionPos, section));
    }

    public void submitRebuild(Vector3i sectionPos) throws InterruptedException {
        ILevel level = Minecraft.getLevel();
        if (level == null) return;

        if (!loadedSections.contains(sectionPos)) return;

        var copyResult = createCopy(sectionPos, level);
        if (copyResult.isEmpty()) {
            if (loadedSections.remove(sectionPos))
                unloadSections.add(sectionPos);

            return;
        }

        var section = copyResult.get();
        sectionMeshQueue.offer(sectionPos, section);
    }

    public void submitNewSection(Vector3i sectionPos) throws InterruptedException {
        ILevel level = Minecraft.getLevel();
        if (level == null) return;

        refreshSections(level);
        if (loadedSections.contains(sectionPos)) return;

        var copyResult = createCopy(sectionPos, level);
        if (copyResult.isEmpty()) return;

        var section = copyResult.get();
        loadedSections.add(section.pos());
        sectionMeshQueue.offer(section.pos(), section);
    }

    public void updateRenderDistance() throws InterruptedException {
        ILevel level = Minecraft.getLevel();
        if (level == null) return;

        if (renderDistanceSupplier.getAsInt() != lastRenderDistance)
            refreshSections(level);

    }

    public static Vector3i getCameraChunkPos() {
        var cameraPos = Minecraft.getCameraPos();
        return new Vector3i(
                (int) cameraPos.x >> 4,
                (int) cameraPos.y >> 4,
                (int) cameraPos.z >> 4
        );
    }

    public class TaskQueue<V> {
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notEmpty = lock.newCondition();
        private final Condition notFull = lock.newCondition();

        private final Map<Vector3i, V> values = new HashMap<>();
        private PendingSection[] queue;

        private int pendingSections = 0;
        private final int maxCapacity;

        private Vector3i lastCameraPos = null;
        private boolean newPending = false;

        private int mod = 0;

        private TaskQueue(int maxCapacity) {
            this.maxCapacity = maxCapacity;
            this.queue = new PendingSection[maxCapacity > 0 ? maxCapacity : 24];
        }

        private void requireCapacity(int newSize) {
            if (maxCapacity > 0) return;
            if (newSize < queue.length) return;

            var newCapacity = Math.max(newSize, queue.length << 1);
            queue = Arrays.copyOf(queue, newCapacity);
        }

        public int size() {
            return pendingSections;
        }

        private void awaitNotEmpty() throws InterruptedException {
            if (pendingSections != 0) return;

            notEmpty.await();
        }

        private void awaitNotFull() throws InterruptedException {
            if (pendingSections != queue.length) return;

            notFull.await();
        }


        private void removeUnloadedSections() {
            int newSize = 0;

            for (int i = 0; i < pendingSections; i++) {
                var entry = queue[i];
                queue[i] = null;

                if (!loadedSections.contains(entry.pos)) {
                    values.remove(entry.pos);
                    continue;
                }

                queue[newSize++] = entry;
            }

            pendingSections = newSize;
        }

        private void sortSections() {
            var cameraChunkPos = getCameraChunkPos();
            if (!newPending && Objects.equals(cameraChunkPos, lastCameraPos)) return;

            newPending = false;
            lastCameraPos = cameraChunkPos;

            removeUnloadedSections();

            mod++;
            Arrays.parallelSort(
                    queue,
                    0,
                    pendingSections,
                    (p1, p2) -> Long.compare(p2.distance(cameraChunkPos, mod), p1.distance(cameraChunkPos, mod))
            );
        }

        public V take() throws InterruptedException {
            lock.lockInterruptibly();

            try {
                while (true) {
                    if (pendingSections == 0) {
                        awaitNotEmpty();
                        continue;
                    }

                    sortSections();
                    if (pendingSections == 0) continue;

                    var top = queue[--pendingSections];
                    queue[pendingSections] = null;

                    notFull.signalAll();

                    return Objects.requireNonNull(values.remove(top.pos));
                }
            } finally {
                lock.unlock();
            }
        }

        public List<V> drain(int maxCount) throws InterruptedException {
            lock.lockInterruptibly();

            try {
                while (true) {
                    if (pendingSections == 0) {
                        awaitNotEmpty();
                        continue;
                    }

                    sortSections();
                    if (pendingSections == 0) continue;

                    int count = Math.min(maxCount, pendingSections);
                    var result = new ArrayList<V>(count);

                    for (int i = 0; i < count; i++) {
                        var top = queue[--pendingSections];
                        queue[pendingSections] = null;

                        result.add(Objects.requireNonNull(values.remove(top.pos)));
                    }

                    notFull.signalAll();

                    return result;
                }
            } finally {
                lock.unlock();
            }
        }

        public void offer(Vector3i sectionCoord, V element) throws InterruptedException {
            lock.lockInterruptibly();

            try {
                while (pendingSections == queue.length)
                    awaitNotFull();

                if (!loadedSections.contains(sectionCoord)) return;

                var previousValue = values.put(sectionCoord, element);
                if (previousValue != null) return;

                requireCapacity(pendingSections + 1);

                queue[pendingSections++] = new PendingSection(sectionCoord);

                newPending = true;
                notEmpty.signalAll();
            } finally {
                lock.unlock();
            }
        }

        public void offerMany(List<Pair<Vector3i, V>> elements) throws InterruptedException {
            lock.lockInterruptibly();

            try {
                int newSectionCount = 0;
                boolean[] newSection = new boolean[elements.size()];

                for (int i = 0; i < elements.size(); i++) {
                    var pair = elements.get(i);

                    var sectionCoord = pair.left();
                    var element = pair.right();

                    if (!loadedSections.contains(sectionCoord)) continue;

                    var previousValue = values.put(sectionCoord, element);
                    if (previousValue != null) continue;

                    newSectionCount++;
                    newSection[i] = true;
                }

                if (newSectionCount == 0) return;
                requireCapacity(pendingSections + newSectionCount);

                for (int i = 0; i < elements.size(); i++) {
                    if (!newSection[i]) continue;

                    while (pendingSections == queue.length)
                        awaitNotFull();

                    queue[pendingSections++] = new PendingSection(elements.get(i).first());
                }

                newPending = true;
                notEmpty.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }

    private static class PendingSection {
        private int mod = -1;
        private long distance = 0;
        public final Vector3i pos;

        public PendingSection(Vector3i section) {
            this.pos = section;
        }

        public long distance(Vector3i cameraPos, int mod) {
            if (this.mod == mod) return distance;

            this.distance = pos.gridDistance(cameraPos);
            this.mod = mod;

            return distance;
        }
    }
}
