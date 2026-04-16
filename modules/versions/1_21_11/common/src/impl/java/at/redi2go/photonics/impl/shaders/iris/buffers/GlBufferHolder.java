package at.redi2go.photonics.impl.shaders.iris.buffers;

import at.redi2go.photonics.api.gpu.buffers.BufferUsage;
import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import at.redi2go.photonics.api.shaders.buffer.IBufferHolder;
import at.redi2go.photonics.impl.mixins.mc.blaze3d.opengl.GlBufferAccessor;
import com.mojang.blaze3d.opengl.GlBuffer;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL43;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class GlBufferHolder implements IBufferHolder {
    private final List<Pair<String, Supplier<IGpuBuffer>>> buffers = new ArrayList<>();
    private final Int2ObjectMap<int[]> foundBlockIndices = new Int2ObjectOpenHashMap<>();

    @Override
    public void addDefaultBuffer(String name, Supplier<IGpuBuffer> buffer) {
        buffers.add(Pair.of(name, buffer));
    }

    public void bind(int shaderId, IntSet usedBuffers) {
        var blockIndices = foundBlockIndices.computeIfAbsent(shaderId, id -> {
            var result = new int[buffers.size()];

            for (int i = 0; i < buffers.size(); i++) {
                var bufferPair = buffers.get(i);
                var name = bufferPair.first();
                var buffer = (GlBuffer) bufferPair.second().get();

                var blockIndex = getBlockIndex(shaderId, name, buffer);
                if (blockIndex == GL31.GL_INVALID_INDEX)
                    throw new IllegalStateException("Block index for " + name + " was GL_INVALID_INDEX");

                result[i] = blockIndex;
            }

            return result;
        });

        int bindingPointIndex = 16;
        for (int i = 0; i < buffers.size(); i++) {
            var bufferPair = buffers.get(i);
            var blockIndex = blockIndices[i];

            while (usedBuffers.contains(--bindingPointIndex)) continue;

            if (bindingPointIndex < 0)
                throw new IllegalStateException("Not enough slots to bind " + buffers.size() + " buffers for Photonics");

            bindBuffer(shaderId, (GlBuffer) bufferPair.second().get(), blockIndex, bindingPointIndex);
        }
    }

    private static int getBlockIndex(int program, String name, GlBuffer buffer) {
        if ((buffer.usage() & BufferUsage.UNIFORM) == 0) {
            return GL43.glGetProgramResourceIndex(program, GL43.GL_SHADER_STORAGE_BLOCK, name);
        } else {
            return GL31.glGetUniformBlockIndex(program, name);
        }
    }

    private static void bindBuffer(int program, GlBuffer buffer, int blockIndex, int bindingPointIndex) {
        int handle = ((GlBufferAccessor) buffer).getHandle();

        if ((buffer.usage() & BufferUsage.UNIFORM) == 0) {
            GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, blockIndex, handle);
            GL43.glShaderStorageBlockBinding(program, blockIndex, bindingPointIndex);
        } else {
            GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, blockIndex, handle);
            GL31.glUniformBlockBinding(program, blockIndex, bindingPointIndex);
        }
    }
}
