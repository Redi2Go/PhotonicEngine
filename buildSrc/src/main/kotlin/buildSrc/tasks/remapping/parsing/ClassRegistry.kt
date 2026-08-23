package buildSrc.tasks.remapping.parsing

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.commons.SimpleRemapper
import java.nio.file.Path
import kotlin.io.path.pathString
import kotlin.io.path.readBytes
import kotlin.sequences.forEach

class ClassRegistry {
    private val _mixins = mutableMapOf<JavaClass, Mixin>()
    private val classes = mutableSetOf<JavaClass>()

    val mixins: Collection<Mixin> get() = _mixins.values

    operator fun contains(type: JavaClass): Boolean {
        return classes.contains(type)
    }

    fun remap(type: JavaClass): JavaClass = _mixins.get(type)?.type ?: type

    fun addAll(files: Sequence<Path>, packagePrefix: JavaPackage) {
        val visitor = MixinVisitor(packagePrefix)

        files.forEach {
            val reader = ClassReader(it.readBytes())
            reader.accept(visitor, ClassReader.SKIP_DEBUG)

            visitor.processResult()
        }
    }

    fun createRemapper(): Remapper {
        return SimpleRemapper(
            Opcodes.ASM9,
            _mixins.entries
                .associate { (key, value) -> key.internalName to value.type.internalName }
        )
    }

    private inner class MixinVisitor(val packagePrefix: JavaPackage) : ClassVisitor(Opcodes.ASM9, null) {
        lateinit var type: JavaClass

        var isMixin = false
        var isRequired = true
        var env = MixinEnv.COMMON

        private fun withEnv(newEnv: MixinEnv) {
            check(env == MixinEnv.COMMON) { "${type.simpleName} is already marked for ${env.name}" }
            env = newEnv
        }

        override fun visit(
            version: Int,
            access: Int,
            name: String,
            signature: String?,
            superName: String?,
            interfaces: Array<out String>?
        ) {
            isMixin = false
            isRequired = true
            env = MixinEnv.COMMON

            type = JavaClass.fromInternalName(name)
        }

        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
            when(Type.getType(descriptor)) {
                MixinType -> isMixin = true

                OptionalType -> isRequired = false

                ClientType -> withEnv(MixinEnv.CLIENT)

                ServerType -> withEnv(MixinEnv.SERVER)
            }

            return null
        }

        fun processResult() {
            if (isMixin) {
                val after = type.moveToMixins(packagePrefix)
                val mixinData = Mixin(after, env, !isRequired)

                val result = _mixins.putIfAbsent(type, mixinData)
                check(result == null) { "duplicate mixin ${type.simpleName}" }

                type = after
            }

            classes.add(type)
        }
    }
}
