package at.redi2go.photonics.core.rendering;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.mc.Minecraft;
import at.redi2go.photonics.api.mc.world.level.ILevel;
import at.redi2go.photonics.api.mc.world.level.chunk.IChunkSection;
import at.redi2go.photonics.core.collect.EmptyQueue;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.NonNls;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.util.ArrayDeque;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntSupplier;

public class SectionManager implements RenderingComponent {
    private Set<Vector2i> loadedChunks = Set.of();
    private final Set<Vector3i> notEmptySections = ConcurrentHashMap.newKeySet();

    private final List<Queue<Vector3i>> unloadQueues = new ArrayList<>();
    private final List<TaskQueue<?>> taskQueues = new ArrayList<>();

    private final IntSupplier renderDistanceSupplier;
    private long remeshCount = 0;

    public SectionManager(IntSupplier renderDistanceSupplier) {
        this.renderDistanceSupplier = renderDistanceSupplier;
    }

    private Vector3i lastCameraPos = null;
    private int lastRenderDistance = -1;

    private void queueUnload(Vector3i section) {
        for (var unloadQueue : unloadQueues)
            unloadQueue.add(section);

        for (var taskQueue : taskQueues) {
            if (taskQueue.tracksUnloads()) {
                try {
                    taskQueue.offerUnload(section);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void queueSection(SectionCopy section) throws InterruptedException {
        for (var taskQueue : taskQueues) {
            if (taskQueue instanceof SectionQueue sectionQueue) {
                try {
                    sectionQueue.offer(section.pos(), section);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
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
                        sectionsToUpdate.add(Pair.of(sectionCoord, new SectionCopy(sectionCoord, section, remeshCount++)));
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

        for (var unloadQueue : unloadQueues)
            unloadQueue.addAll(unloadedSections);

        for (var taskQueue : taskQueues) {
            taskQueue.lock.lockInterruptibly();

            try {
                if (taskQueue.tracksUnloads())
                    taskQueue.offerUnloads(unloadedSections);

                if (taskQueue instanceof SectionQueue sectionQueue)
                    sectionQueue.offerMany(sectionsToUpdate);
            } finally {
                taskQueue.lock.unlock();
            }
        }
    }

    public Queue<Vector3i> newUnloadQueue() {
        var queue = new ConcurrentLinkedQueue<Vector3i>();
        unloadQueues.add(queue);

        return queue;
    }

    public SectionQueue newSectionQueue(boolean trackUnloads) {
        var queue = new SectionQueue(-1, trackUnloads);
        taskQueues.add(queue);

        return queue;
    }

    public <T> TaskQueue<T> newTaskQueue(int maxCapacity, boolean trackUnloads) {
        var queue = new TaskQueue<T>(maxCapacity, trackUnloads);
        taskQueues.add(queue);

        return queue;
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

        return Optional.of(new SectionCopy(sectionPos, section, remeshCount++));
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
            if (!notEmptySections.contains(sectionPos)) {
                onSectionAdded(x, y, z);
                return;
            }

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
        private final Lock lock = new ReentrantLock();

        private final Condition notEmptyCondition = lock.newCondition();
        private final Condition notFullCondition = lock.newCondition();

        private int size = 0;
        private final int capacity;

        private SectionInfo[] sectionQueue;
        private final Map<Vector3i, V> sectionValues = new HashMap<>();

        private final Queue<Vector3i> unloadedQueue;

        private Vector3i lastCameraPos = null;
        private int lastRenderDistance = 0;
        private boolean newPending = false;
        private int mod = 0;

        private TaskQueue(int maxSize, boolean trackUnloads) {
            this.capacity = maxSize;
            this.sectionQueue = new SectionInfo[maxSize > 0 ? maxSize : 24];

            unloadedQueue = trackUnloads ? new ArrayDeque<>() : EmptyQueue.of();
        }

        private boolean tracksUnloads() {
            return unloadedQueue != EmptyQueue.<Vector3i>of();
        }

        public void awaitTask() throws InterruptedException {
            lock.lockInterruptibly();

            try {
                while (true) {
                    if (size != 0 || !unloadedQueue.isEmpty()) return;

                    notEmptyCondition.await();
                }
            } finally {
                lock.unlock();
            }
        }


        public List<Vector3i> drainUnloadQueue() throws InterruptedException {
            lock.lockInterruptibly();

            try {
                if (unloadedQueue.isEmpty()) return List.of();

                List<Vector3i> result = new ArrayList<>(unloadedQueue.size());

                while (!unloadedQueue.isEmpty()) {
                    var section = unloadedQueue.remove();
                    if (notEmptySections.contains(section)) continue;

                    result.add(section);
                }

                return result;
            } finally {
                lock.unlock();
            }
        }

        public Optional<V> take() throws InterruptedException {
            lock.lockInterruptibly();

            try {
                if (size == 0) return Optional.empty();

                sortSections();
                if (size == 0) return Optional.empty();

                notFullCondition.signalAll();
                return Optional.of(removeFirst());
            } finally {
                lock.unlock();
            }
        }

        public List<V> drain(int maxCount) throws InterruptedException {
            lock.lockInterruptibly();

            try {
                if (size == 0) return List.of();

                sortSections();
                if (size == 0) return List.of();

                int count = Math.min(maxCount, size);
                var result = new ArrayList<V>(count);

                for (int i = 0; i < count; i++)
                    result.add(removeFirst());

                notFullCondition.signalAll();

                return result;
            } finally {
                lock.unlock();
            }
        }


        public void offer(Vector3i sectionCoord, V element) throws InterruptedException {
            lock.lockInterruptibly();

            try {
                while (size == capacity)
                    notFullCondition.await();

                if (!notEmptySections.contains(sectionCoord)) return;

                boolean[] shouldAppendSection = {false};
                sectionValues.compute(sectionCoord, (k, previous) -> {
                    if (previous == null) {
                        shouldAppendSection[0] = true;
                        return element;
                    }

                    long previousPriority = previous instanceof PrioritizedTask p1 ? p1.priority() : Long.MIN_VALUE;
                    long newPriority = element instanceof PrioritizedTask p2 ? p2.priority() : Long.MIN_VALUE;

                    if (previousPriority > newPriority) {
                        tryClose(element);
                        return previous;
                    } else {
                        tryClose(previous);
                        return element;
                    }
                });

                if (!shouldAppendSection[0]) return;

                requireCapacity(size + 1);

                sectionQueue[size++] = new SectionInfo(sectionCoord);

                newPending = true;
                notEmptyCondition.signalAll();
            } finally {
                lock.unlock();
            }
        }

        public void offerMany(List<Pair<Vector3i, V>> elements) throws InterruptedException {
            if (elements.isEmpty()) return;
            lock.lockInterruptibly();

            try {
                int index = 0;
                while (index < elements.size()) {
                    while (size == capacity)
                        notFullCondition.await();

                    int maxRemainingSlots = capacity > 0 ? (capacity - size) : elements.size();
                    requireCapacity(size + maxRemainingSlots);

                    boolean changed = false;
                    for (int i = 0; i < maxRemainingSlots; i++) {
                        var elementPair = elements.get(index + i);

                        var sectionCoord = elementPair.left();
                        var element = elementPair.right();

                        var previousValue = sectionValues.put(sectionCoord, element);
                        if (previousValue != null) continue;

                        sectionQueue[size++] = new SectionInfo(sectionCoord);
                        changed = true;
                    }

                    if (changed) {
                        newPending = true;
                        notEmptyCondition.signalAll();
                    }

                    index += maxRemainingSlots;
                }
            } finally {
                lock.unlock();
            }
        }

        private void offerUnload(Vector3i section) throws InterruptedException {
            lock.lockInterruptibly();

            try {
                if (unloadedQueue.add(section))
                    notEmptyCondition.signalAll();
            } finally {
                lock.unlock();
            }
        }

        private void offerUnloads(Collection<Vector3i> sections) throws InterruptedException {
            lock.lockInterruptibly();

            try {
                if (unloadedQueue.addAll(sections))
                    notEmptyCondition.signalAll();
            } finally {
                lock.unlock();
            }
        }

        private void requireCapacity(int newSize) {
            if (capacity > 0) return;
            if (newSize < sectionQueue.length) return;

            var newCapacity = Math.max(newSize, sectionQueue.length << 1);
            sectionQueue = Arrays.copyOf(sectionQueue, newCapacity);
        }

        private void trimUnloadedSections() {
            int newSize = 0;

            for (int i = 0; i < size; i++) {
                var entry = sectionQueue[i];
                sectionQueue[i] = null;

                if (!notEmptySections.contains(entry.pos)) {
                    sectionValues.remove(entry.pos);
                    continue;
                }

                sectionQueue[newSize++] = entry;
            }

            if (size != newSize && capacity > 0)
                notFullCondition.signalAll();

            size = newSize;
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

            trimUnloadedSections();

            mod++;
            Arrays.parallelSort(
                    sectionQueue,
                    0,
                    size,
                    (p1, p2) -> Long.compare(p2.distance(cameraChunkPos, mod), p1.distance(cameraChunkPos, mod))
            );
        }

        private V removeFirst() {
            var top = sectionQueue[--size];
            sectionQueue[size] = null;

            return Objects.requireNonNull(sectionValues.remove(top.pos));
        }

        private static void tryClose(Object value) {
            if (value instanceof Disposable disposable)
                disposable.close();
        }

    }

    public class SectionQueue extends TaskQueue<SectionCopy> {
        private SectionQueue(int maxSize, boolean trackUnloads) {
            super(maxSize, trackUnloads);
        }
    }

    private static class SectionInfo {
        private int mod = -1;
        private long distance = 0;
        public final Vector3i pos;

        public SectionInfo(Vector3i section) {
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

