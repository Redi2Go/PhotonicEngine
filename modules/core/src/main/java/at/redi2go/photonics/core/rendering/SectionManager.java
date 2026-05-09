package at.redi2go.photonics.core.rendering;

import at.redi2go.photonics.api.mc.Minecraft;
import at.redi2go.photonics.api.mc.world.level.ILevel;
import at.redi2go.photonics.api.mc.world.level.chunk.IChunkSection;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.NonNls;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

public class SectionManager implements RenderingComponent {
    private Set<Vector2i> loadedChunks = Set.of();
    private final Set<Vector3i> notEmptySections = ConcurrentHashMap.newKeySet();

    private final List<Queue<Vector3i>> unloadQueues = new ArrayList<>();
    private final List<TaskQueue<SectionCopy>> sectionQueues = new ArrayList<>();

    private final IntSupplier renderDistanceSupplier;

    public SectionManager(IntSupplier renderDistanceSupplier) {
        this.renderDistanceSupplier = renderDistanceSupplier;
    }

    private Vector3i lastCameraPos = null;
    private int lastRenderDistance = -1;

    private void queueUnload(Vector3i section) {
        for (var unloadQueue : unloadQueues)
            unloadQueue.add(section);
    }

    private void queueUnload(Collection<Vector3i> sections) {
        for (var unloadQueue : unloadQueues)
            unloadQueue.addAll(sections);
    }

    private void queueSection(SectionCopy section) throws InterruptedException {
        for (var sectionQueue : sectionQueues)
            sectionQueue.offer(section.pos(), section);
    }

    private void queueSections(List<Pair<Vector3i, SectionCopy>> sections) throws InterruptedException {
        for (var sectionQueue : sectionQueues)
            sectionQueue.offerMany(sections);
    }

    private void refreshSections(@NonNls ILevel level) throws InterruptedException {
        var cameraPos = getCameraChunkPos();
        int rd = renderDistanceSupplier.getAsInt();

        if (rd == lastRenderDistance && Objects.equals(lastCameraPos, cameraPos)) return;

        Set<Vector2i> discoveredChunks = new HashSet<>();
        Set<Vector3i> discoveredSections = new HashSet<>();
        Set<Vector3i> unloadedSections = new HashSet<>();
        List<Pair<Vector3i, SectionCopy>> sectionsToUpdate = new ArrayList<>();

        for (int px = -rd; px <= rd; px++) {
            for (int pz = -rd; pz <= rd; pz++) {
                int sectionX = cameraPos.x + px;
                int sectionZ = cameraPos.z + pz;

                discoveredChunks.add(new Vector2i(sectionX, sectionZ));

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

                    if (!notEmptySections.contains(sectionCoord))
                        sectionsToUpdate.add(Pair.of(sectionCoord, new SectionCopy(sectionCoord, section)));
                }
            }
        }

        for (var itr = notEmptySections.iterator(); itr.hasNext(); ) {
            var section = itr.next();
            if (discoveredSections.contains(section)) continue;

            itr.remove();
            unloadedSections.add(section);
        }

        notEmptySections.addAll(discoveredSections);

        loadedChunks = discoveredChunks;

        lastCameraPos = cameraPos;
        lastRenderDistance = rd;

        queueUnload(unloadedSections);
        queueSections(sectionsToUpdate);
    }

    public Queue<Vector3i> newUnloadQueue() {
        var queue = new ConcurrentLinkedQueue<Vector3i>();
        unloadQueues.add(queue);

        return queue;
    }

    public TaskQueue<SectionCopy> newSectionQueue() {
        var queue = new TaskQueue<SectionCopy>(-1);
        sectionQueues.add(queue);

        return queue;
    }

    public <T> TaskQueue<T> newTaskQueue(int maxCapacity) {
        return new TaskQueue<>(maxCapacity);
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

    @Override
    public void onFrameBegin() {
        ILevel level = Minecraft.getLevel();
        if (level == null) return;

        try {
            if (renderDistanceSupplier.getAsInt() != lastRenderDistance) {
                refreshSections(level);
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void onSectionAdded(int x, int y, int z) {
        ILevel level = Minecraft.getLevel();
        if (level == null) return;

        try {
            refreshSections(level);
            Vector3i sectionPos = new Vector3i(x, y, z);

            if (!loadedChunks.contains(new Vector2i(x, z))) return;
            if (notEmptySections.contains(sectionPos)) return;

            var copyResult = createCopy(sectionPos, level);
            if (copyResult.isEmpty()) return;

            var section = copyResult.get();
            notEmptySections.add(section.pos());
            queueSection(section);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void onSectionChanged(int x, int y, int z) {
        ILevel level = Minecraft.getLevel();
        if (level == null) return;

        try {
            Vector3i sectionPos = new Vector3i(x, y, z);
            if (!notEmptySections.contains(sectionPos)) return;

            var copyResult = createCopy(sectionPos, level);
            if (copyResult.isEmpty()) {
                if (notEmptySections.remove(sectionPos))
                    queueUnload(sectionPos);

                return;
            }

            var section = copyResult.get();
            queueSection(section);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
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
        private int lastRenderDistance = 0;

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

                if (!notEmptySections.contains(entry.pos)) {
                    values.remove(entry.pos);
                    continue;
                }

                queue[newSize++] = entry;
            }

            if (pendingSections != newSize)
                notFull.signalAll();

            pendingSections = newSize;
        }

        private void sortSections() {
            var cameraChunkPos = getCameraChunkPos();

            if (
                    !newPending &&
                            SectionManager.this.lastRenderDistance == lastRenderDistance &&
                            Objects.equals(cameraChunkPos, lastCameraPos)
            ) return;

            newPending = false;
            lastCameraPos = cameraChunkPos;
            lastRenderDistance = SectionManager.this.lastRenderDistance;

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

                if (!notEmptySections.contains(sectionCoord)) return;

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

                    if (!notEmptySections.contains(sectionCoord)) continue;

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
