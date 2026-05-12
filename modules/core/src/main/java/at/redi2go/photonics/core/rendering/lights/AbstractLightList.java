package at.redi2go.photonics.core.rendering.lights;

import at.redi2go.photonics.api.mc.Minecraft;
import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.config.PhConfig;
import at.redi2go.photonics.core.config.PhConfigWatcher;
import at.redi2go.photonics.core.config.lights.LightRegistry;
import at.redi2go.photonics.core.iris.pipeline.uniform.IDynamicUniformHolder;
import at.redi2go.photonics.core.rendering.RenderingComponent;
import at.redi2go.photonics.core.rendering.SectionCopy;
import at.redi2go.photonics.core.rendering.SectionManager;
import at.redi2go.photonics.core.rendering.UniformUpdater;
import at.redi2go.photonics.core.rendering.world.WorldOrigin;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public abstract class AbstractLightList<S> implements Runnable, RenderingComponent {
    private final Thread compilerThread;
    private final ReentrantLock lock = new ReentrantLock();

    private boolean needsReload = false;
    private LightRegistry lightRegistry;
    private final PhConfigWatcher<LightRegistry> lightsRegistryObserver;

    private final int maxLights;
    private final Supplier<WorldOrigin> worldOriginSupplier;

    private final Queue<Vector3i> unloadedQueue;
    private final SectionManager.TaskQueue<SectionCopy> sectionQueue;

    private final ListMultimap<Vector3i, TracedLightPosition> tracedLightPositions;

    protected final TracedLightPosition[] lights;
    protected WorldOrigin worldOrigin = null;
    protected int lightCount = 0;

    private final UniformUpdater uniformUpdater = new UniformUpdater();

    protected WorldOrigin mostRecentOrigin = null;
    protected int mostRecentLightCount = 0;

    @SuppressWarnings("UnstableApiUsage")
    public AbstractLightList(
            SectionManager sectionManager,
            int maxLights,
            Supplier<WorldOrigin> worldOriginSupplier
    ) {
        this.maxLights = maxLights;
        this.worldOriginSupplier = worldOriginSupplier;

        this.unloadedQueue = sectionManager.newUnloadQueue();
        this.sectionQueue = sectionManager.newSectionQueue();

        this.tracedLightPositions = MultimapBuilder.hashKeys()
                .arrayListValues()
                .build();

        this.lights = new TracedLightPosition[maxLights];

        this.compilerThread = new Thread(this, "Photonics Light List Compiler");
        this.compilerThread.start();

        this.lightRegistry = PhConfig.getLightRegistry();
        this.lightsRegistryObserver = PhConfig.watch(
                ignored -> PhConfig.getLightRegistry(),
                this::setLightRegistry
        );
    }

    @Override
    public void run() {
        while (true) {
            if (Thread.interrupted() && !needsReload) return;

            try {
                unloadSections();
                reloadLights();

                addNewSections();
                trimLights();

                storeLights();
            } catch (InterruptedException e) {
                if (!needsReload) return;
            }
        }
    }

    // Compiler stages

    private void unloadSections() {
        while (!unloadedQueue.isEmpty()) {
            tracedLightPositions.removeAll(unloadedQueue.remove());
        }
    }

    private void reloadLights() {
        if (!needsReload) return;
        needsReload = false;

        for (var section : tracedLightPositions.keySet()) {
            var lights = tracedLightPositions.get(section);

            for (var itr = lights.listIterator(); itr.hasNext(); ) {
                var lightPosition = itr.next();
                var newLightInfo = lightRegistry.get(lightPosition.blockState());

                if (newLightInfo != null) {
                    itr.set(new TracedLightPosition(
                            lightPosition.blockId(),
                            lightPosition.pos(),
                            lightPosition.blockState(),
                            newLightInfo
                    ));
                } else itr.remove();
            }
        }
    }

    private void addNewSections() throws InterruptedException {
        var newSections = sectionQueue.drain(Integer.MAX_VALUE);
        for (var section : newSections) {
            var lights = tracedLightPositions.get(section.pos());
            lights.clear();

            section.forEachBlock((blockChunkOffset, blockPos, block) -> {
                var light = lightRegistry.get(block);
                if (light == null) return;

                lights.add(
                        new TracedLightPosition(
                                -1, //TODO: Get block id
                                new Vector3d(blockPos.x(), blockPos.y(), blockPos.z()).add(0.5, 0.5, 0.5),
                                block,
                                light
                        )
                );
            });

            if (lights.isEmpty())
                tracedLightPositions.removeAll(section.pos());
        }
    }

    private void trimLights() {
        var loadedLights = tracedLightPositions.values().toArray(TracedLightPosition[]::new);
        int oldSize = lightCount;
        int newSize;

        if (loadedLights.length < maxLights) {
            newSize = loadedLights.length;

            if (oldSize > newSize)
                Arrays.fill(lights, newSize, oldSize, null);
        } else {
            newSize = maxLights;

            Vector3d cameraPosition = Minecraft.getCameraPos();
            int mod = (int) System.nanoTime();

            Arrays.sort(
                    loadedLights,
                    Comparator.comparingDouble(light -> light.getLuminance(cameraPosition, mod))
            );
        }

        System.arraycopy(loadedLights, 0, this.lights, 0, newSize);
        this.lightCount = newSize;
    }

    protected abstract S getStorage();

    protected abstract void storeLight(S storage, int index, Vector4f[] light);

    protected abstract void markForUpload(S storage);

    private void storeLights() throws InterruptedException {
        lock.lockInterruptibly();

        try {
            S storage = getStorage();
            worldOrigin = WorldOrigin.get();

            for (int i = 0; i < lightCount; i++) {
                var light = lights[i];

                storeLight(
                        storage,
                        i,
                        light.lightInfo().toVector4Array(
                                new Vector3f(worldOrigin.applyOffset(light.pos())),
                                light.blockId()
                        )
                );
            }

            markForUpload(storage);
            uniformUpdater.updateNextFrame();
        } finally {
            lock.unlock();
        }
    }

    private void setLightRegistry(LightRegistry lightRegistry) {
        this.lightRegistry = lightRegistry;

        needsReload = true;
        compilerThread.interrupt();
    }

    // Upload

    protected abstract void upload();

    @Override
    public void onFrameBegin() {
        lock.lock();

        try {
            upload();
            uniformUpdater.updateAll();

            mostRecentOrigin = worldOrigin;
            mostRecentLightCount = lightCount;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void registerDynamicUniforms(IDynamicUniformHolder dynamicUniforms) {
        dynamicUniforms.uniform3f("light_list_offset", () -> {
            var listOrigin = mostRecentOrigin;
            var realOrigin = worldOriginSupplier.get();

            if (listOrigin == null || realOrigin == null)
                return new Vector3f(0f);

            return new Vector3f(realOrigin.sub(listOrigin, new Vector3d()));
        }, uniformUpdater.newNotifier());

        dynamicUniforms.uniform1i("ph_light_count", () -> mostRecentLightCount, uniformUpdater.newNotifier());
    }

    @Override
    public void close() {
        needsReload = false;
        compilerThread.interrupt();

        lightsRegistryObserver.close();
    }
}
