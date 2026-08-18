package buildSrc.tasks.remapping

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.commons.SimpleRemapper
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readBytes
import kotlin.io.path.relativeTo
import kotlin.io.path.walk
import kotlin.sequences.forEach

@JvmInline
value class MixinClasses private constructor(private val mapping: MutableMap<Type, Type>) {
    constructor() : this(mutableMapOf())

    constructor(mixinDir: Path, packagePrefix: String) : this() {
        var mixinDir = mixinDir.absolute()

        for (clazz in mixinDir.walk()) {
            val path = clazz.absolute().relativeTo(mixinDir)

            val basePackage = path.toString().substringBeforeLast('/').removeSurrounding("/").replace('/', '.')
            val className = path.nameWithoutExtension
            if (className.isEmpty()) continue

            mapping.put(
                typeFromImport(packagePrefix, MIXIN_PACKAGE, basePackage, className),
                typeFromImport(packagePrefix, basePackage, className)
            )
        }
    }

    fun remap(before: Type, packagePrefix: String) {
        mapping[before] = typeFromImport(
            packagePrefix,
            MIXIN_PACKAGE,
            before.packageName.removePrefix("$packagePrefix."),
            before.simpleName
        )
    }

    fun remapAll(files: Sequence<Path>, packagePrefix: String) {
        val visitor = object : ClassTypeVisitor(Opcodes.ASM9) {
            override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
                if (Type.getType(descriptor) == MixinType)
                    remap(type, packagePrefix)

                return super.visitAnnotation(descriptor, visible)
            }
        }

        files.forEach {
            val reader = ClassReader(it.readBytes())
            reader.accept(visitor, ClassReader.SKIP_DEBUG)
        }
    }

    fun createRemapper(): Remapper {
        return SimpleRemapper(
            Opcodes.ASM9,
            mapping.entries
                .associate { (key, value) -> key.internalName to value.internalName }

        )
    }
}
