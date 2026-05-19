package at.redi2go.photonics.core.rendering.lights;

import at.redi2go.photonics.api.mc.Id;
import at.redi2go.photonics.api.mc.Minecraft;
import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlock;
import at.redi2go.photonics.api.mc.world.level.ILevel;
import at.redi2go.photonics.api.mc.world.level.chunk.IChunkSection;
import at.redi2go.photonics.api.shaders.IShaderPack;
import at.redi2go.photonics.core.config.PhConfig;
import at.redi2go.photonics.core.config.PhConfigWatcher;
import at.redi2go.photonics.core.config.lights.LightRegistry;
import at.redi2go.photonics.core.iris.pipeline.uniform.IDynamicUniformHolder;
import at.redi2go.photonics.core.iris.pipeline.uniform.IUniformHolder;
import at.redi2go.photonics.core.iris.pipeline.uniform.IUniformUpdateFrequency;
import at.redi2go.photonics.core.rendering.RenderingComponent;
import at.redi2go.photonics.core.rendering.SectionCopy;
import at.redi2go.photonics.core.rendering.SectionManager;
import at.redi2go.photonics.core.rendering.UniformUpdater;
import at.redi2go.photonics.core.rendering.WorldOrigin;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public abstract class AbstractLightList implements Runnable, RenderingComponent {
    private static final int MAX_SECTIONS_PER_RUN = 48;
    private static final IBlock BLOCK_LAVA = IBlock.fromIdOrThrow(Id.fromNamespaceAndPath("minecraft", "lava"));

    private final Thread compilerThread;
    private final ReentrantLock lock = new ReentrantLock();

    private boolean needsReload = false;
    private LightRegistry lightRegistry;
    private final PhConfigWatcher<LightRegistry> lightsRegistryObserver;

    private final int maxLights;
    private final Supplier<WorldOrigin> worldOriginSupplier;

    private final SectionManager.SectionQueue sectionQueue;

    private final Object2LongMap<Vector3i> sectionHashes = new Object2LongOpenHashMap<>();

    private final ListMultimap<Vector3i, TracedLightPosition> tracedLightPositions;
    private final UniformUpdater uniformUpdater = new UniformUpdater();

    protected LightList lights;
    protected LightList mostRecentLights;

    @SuppressWarnings("UnstableApiUsage")
    public AbstractLightList(
            SectionManager sectionManager,
            int maxLights,
            Supplier<WorldOrigin> worldOriginSupplier
    ) {
        this.maxLights = maxLights;
        this.worldOriginSupplier = worldOriginSupplier;

        this.sectionQueue = sectionManager.newSectionQueue(true);

        this.tracedLightPositions = MultimapBuilder.hashKeys()
                .arrayListValues()
                .build();

        this.compilerThread = new Thread(this, "Photonics Light List Compiler");
        this.compilerThread.start();

        this.lightRegistry = PhConfig.getLightRegistry();
        this.lightsRegistryObserver = PhConfig.watch(
                ignored -> PhConfig.getLightRegistry(),
                this::setLightRegistry
        );
    }

    private void setLightRegistry(LightRegistry lightRegistry) {
        this.lightRegistry = lightRegistry;

        needsReload = true;
        compilerThread.interrupt();
    }

    @Override
    public void run() {
        while (true) {
            if (Thread.interrupted() && !needsReload) return;

            try {
                sectionQueue.awaitTask();

                boolean needsUpload = false;

                var unloadedSections = sectionQueue.drainUnloadQueue();
                if (!unloadedSections.isEmpty()) {
                    needsUpload = true;
                    unloadSections(unloadedSections);
                }


                needsUpload |= reloadLights();

                var loadedSections = sectionQueue.drain(MAX_SECTIONS_PER_RUN);
                if (!loadedSections.isEmpty() && addNewSections(loadedSections))
                    needsUpload = true;

                if (needsUpload) {
                    var newLights = trimLights();
                    storeLights(newLights);
                }
            } catch (InterruptedException e) {
                if (!needsReload) return;
            }
        }
    }

    // Compiler stages

    private void unloadSections(List<Vector3i> unloadedSections) {
        for (var section : unloadedSections) {
            tracedLightPositions.removeAll(section);
            sectionHashes.removeLong(section);
        }
    }

    private boolean reloadLights() {
        if (!needsReload) return false;
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

        return true;
    }

    private boolean addNewSections(List<SectionCopy> newSections) {
        var level = Minecraft.getLevel();
        if (level == null) return false;

        var shaderPack = IShaderPack.getCurrentPack();
        boolean changed = false;

        for (var section : newSections) {
            var sectionHash = section.computeSectionHash();
            if (sectionHashes.put(section.pos(), sectionHash) == sectionHash) continue;

            changed = true;
            var lights = tracedLightPositions.get(section.pos());
            lights.clear();

            section.forEachBlock((blockChunkOffset, blockPos, block) -> {
                var light = lightRegistry.get(block);
                if (light == null || !light.isTraced()) return;

                if (!shouldCullLight(section, level, blockPos))
                    lights.add(
                            new TracedLightPosition(
                                    shaderPack.map(e -> e.getBlockId(block)).orElse(-1),
                                    new Vector3d(blockPos.x(), blockPos.y(), blockPos.z()).add(0.5, 0.5, 0.5),
                                    block,
                                    light
                            )
                    );
            });

            if (lights.isEmpty())
                tracedLightPositions.removeAll(section.pos());
        }

        return changed;
    }

    private boolean shouldCullLight(
            SectionCopy blockOwner,
            ILevel level,
            IBlockPos blockPos
    ) {
        IChunkSection section = blockOwner;
        Vector3i sectionPos = blockOwner.pos();

        for (var offset : NEIGHBORS) {
            var neighborBlockPos = blockPos.offset(offset);
            var blockSectionPos = SectionCopy.getSectionCoord(neighborBlockPos);

            if (!blockSectionPos.equals(sectionPos)) {
                if (blockSectionPos.equals(blockOwner.pos())) {
                    section = blockOwner;
                } else {
                    var chunkAccess = level.getChunkOrNull(sectionPos.x, sectionPos.z);
                    if (chunkAccess == null) continue;

                    var newSection = chunkAccess.sections()[level.getSectionIndexFromSectionY(sectionPos.y)];
                    if (newSection == null) continue;

                    section = newSection;
                }
            }

            if (section.hasOnlyAir()) continue;

            var blockState = section.getBlockState(
                    neighborBlockPos.x() & 15,
                    neighborBlockPos.y() & 15,
                    neighborBlockPos.z() & 15
            );

            if (blockState.is(BLOCK_LAVA)) continue;

            if (blockState.isAir() || !blockState.isSuffocating(level, blockPos) || !blockState.isCollisionShapeFullBlock(level, blockPos))
                return false;
        }

        return true;
    }

    private LightList trimLights() {
        var loadedLights = tracedLightPositions.values().toArray(TracedLightPosition[]::new);
        if (loadedLights.length < maxLights) {
            return new LightList(loadedLights, WorldOrigin.get());
        }

        Vector3d cameraPosition = Minecraft.getCameraPos();
        int mod = (int) System.nanoTime();

        Arrays.sort(
                loadedLights,
                Comparator.comparingDouble(light -> -light.getLuminance(cameraPosition, mod))
        );

        return new LightList(
                Arrays.copyOf(loadedLights, maxLights),
                WorldOrigin.get()
        );
    }

    protected abstract void storeLight(int index, Vector4f[] light);

    protected abstract void storeMapping(int beforeIndex, int afterIndex);

    protected abstract void clearMapping();

    protected abstract void prepareUpload();

    private void storeLights(LightList lights) throws InterruptedException {
        if (Objects.equals(mostRecentLights, lights)) return;

        lock.lockInterruptibly();

        try {
            this.lights = lights;
            var worldOrigin = lights.origin();

            for (int i = 0; i < lights.size(); i++) {
                var light = lights.get(i);

                storeLight(
                        i,
                        light.lightInfo().toVector4Array(
                                new Vector3f(worldOrigin.applyOffset(light.pos())),
                                light.blockId()
                        )
                );
            }

            if (mostRecentLights != null)
                lights.createMapping(mostRecentLights)
                        .forEachIndex(this::storeMapping);

            prepareUpload();
            uniformUpdater.updateNextFrame();
        } finally {
            lock.unlock();
        }
    }

    // Upload

    protected abstract void upload();

    @Override
    public void onFrameBegin() {
        lock.lock();

        try {
            upload();
            uniformUpdater.updateAll();

            if (lights != mostRecentLights) {
                mostRecentLights = lights;
                clearMapping();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void registerUniforms(IUniformHolder uniforms) {
        uniforms.uniform3f(IUniformUpdateFrequency.perFrame(), "light_list_offset", () -> {
            var listOrigin = mostRecentLights == null ? null : mostRecentLights.origin();
            var realOrigin = worldOriginSupplier.get();

            if (listOrigin == null || realOrigin == null)
                return new Vector3f(0f);

            return new Vector3f(realOrigin.sub(listOrigin, new Vector3d()));
        });
    }

    @Override
    public void registerDynamicUniforms(IDynamicUniformHolder dynamicUniforms) {
        dynamicUniforms.uniform1i("light_list_size", () -> mostRecentLights == null ? 0 : mostRecentLights.size(), uniformUpdater.newNotifier());
    }

    @Override
    public void close() {
        needsReload = false;
        compilerThread.interrupt();

        lightsRegistryObserver.close();
    }

    private static final Vector3i[] NEIGHBORS = new Vector3i[]{
            new Vector3i(0, 1, 0),
            new Vector3i(0, -1, 0),

            new Vector3i(1, 0, 0),
            new Vector3i(-1, 0, 0),

            new Vector3i(0, 0, 1),
            new Vector3i(0, 0, -1),
    };
}
