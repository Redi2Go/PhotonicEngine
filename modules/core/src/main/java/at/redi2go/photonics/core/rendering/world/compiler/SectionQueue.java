package at.redi2go.photonics.core.rendering.world.compiler;

import at.redi2go.photonics.api.mc.Minecraft;
import org.joml.Vector3i;

import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SectionQueue {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();

    private PendingSection[] sections = new PendingSection[24];
    private int pendingSections = 0;


    private int mod = 0;

    public int size() {
        return pendingSections;
    }

    private void awaitNotEmpty() throws InterruptedException {
        if (pendingSections != 0) return;

        notEmpty.await();
    }

    private void sortSections() {
        mod++;

        var cameraPos = Minecraft.getCameraPos();
        var cameraChunkPos = new Vector3i(
                (int) cameraPos.x >> 4,
                (int) cameraPos.y >> 4,
                (int) cameraPos.z >> 4
        );

        Arrays.parallelSort(
                sections,
                0,
                pendingSections,
                (p1, p2) -> Long.compare(p2.distance(cameraChunkPos, mod), p1.distance(cameraChunkPos, mod))
        );
    }

    public Vector3i takeSection() throws InterruptedException {
        lock.lockInterruptibly();

        try {
            while (pendingSections == 0)
                awaitNotEmpty();

            sortSections();

            var top = sections[--pendingSections];
            sections[pendingSections] = null;

            return top;
        } finally {
            lock.unlock();
        }
    }

    public void submitSection(Vector3i sectionPos) {
        lock.lock();

        try {
             if (pendingSections == sections.length)
                 sections = Arrays.copyOf(sections, sections.length << 1);

             sections[pendingSections++] = new PendingSection(sectionPos);
             notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private static class PendingSection extends Vector3i {
        private int mod = -1;
        private long distance = 0;

        public long distance(Vector3i cameraPos, int mod) {
            if (this.mod == mod) return distance;

            this.distance = gridDistance(cameraPos);
            this.mod = mod;

            return distance;
        }

        public PendingSection(Vector3i other) {
            super(other);
        }
    }
}
