package buildSrc.tasks.remapping.parsing

import buildSrc.tasks.remapping.MIXIN_PACKAGE
import org.objectweb.asm.Type
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.relativeTo

class JavaPackage private constructor(private val parts: Array<String>, val isMixin: Boolean) {
    private constructor(parts: Array<String>) : this(parts, parts.contains(MIXIN_PACKAGE))

    val nameCount: Int get() = parts.size

    val internalName: String get() = parts.joinToString { "/" }

    fun startsWith(prefix: JavaPackage): Boolean {
        if (prefix.nameCount > nameCount) return false;

        for (i in prefix.parts.indices) {
            if (prefix.parts[i] != parts[i])
                return false;
        }

        return true;
    }

    operator fun div(other: String): JavaPackage = JavaPackage(parts.plus(other), isMixin)

    fun moveToMixins(packagePrefix: JavaPackage): JavaPackage {
        check(startsWith(packagePrefix))
        if (isMixin) return this;

        val newParts = arrayOfNulls<String>(nameCount + 1)
        newParts[packagePrefix.nameCount] = MIXIN_PACKAGE

        parts.copyInto(newParts, endIndex = packagePrefix.nameCount)
        parts.copyInto(newParts, destinationOffset = packagePrefix.nameCount + 1, startIndex = packagePrefix.nameCount)

        @Suppress("UNCHECKED_CAST")
        return JavaPackage(newParts as Array<String>, true)
    }

    fun toPath(root: Path): Path = parts.fold(root, Path::div)

    override fun hashCode(): Int = parts.contentHashCode()

    override fun equals(other: Any?): Boolean = other is JavaPackage && parts.contentEquals(other.parts)

    override fun toString(): String = parts.joinToString(".")

    companion object {
        fun fromImport(value: String): JavaPackage {
            return JavaPackage(value.split('.').toTypedArray())
        }

        fun fromRelativePath(path: Path): JavaPackage {
            check(!path.isAbsolute) { "absolute path" }

            return JavaPackage(
                Array(path.nameCount - if (!path.extension.isEmpty()) 1 else 0) {
                    path.getName(it).toString()
                }
            )
        }

        fun fromPath(path: Path, root: Path): JavaPackage {
            return fromRelativePath(path.relativeTo(root))
        }

        fun fromInternalName(internalName: String): JavaPackage {
            return JavaPackage(
                internalName.substringBeforeLast('/')
                    .split('/')
                    .toTypedArray(),
            )
        }
    }
}

data class JavaClass(val packageName: JavaPackage, val simpleName: String) {
    val isMixin: Boolean
        get() = packageName.isMixin

    val internalName: String
        get() =  "${packageName.internalName}/$simpleName"

    fun moveToMixins(packagePrefix: JavaPackage): JavaClass =
        JavaClass(packageName.moveToMixins(packagePrefix), simpleName)

    fun toPath(root: Path): Path =
        packageName.toPath(root) / "$simpleName.class"

    fun toType(): Type = Type.getObjectType(simpleName)

    companion object {
        fun fromRelativePath(path: Path): JavaClass {
            check(!path.isAbsolute) { "absolute path" }
            check(path.extension.isNotEmpty()) { "not a file" }

            return JavaClass(
                JavaPackage.fromRelativePath(path),
                path.nameWithoutExtension
            )
        }

        fun fromPath(path: Path, root: Path): JavaClass {
            return fromRelativePath(path.relativeTo(root))
        }

        fun fromInternalName(internalName: String): JavaClass {
            return JavaClass(
                JavaPackage.fromInternalName(internalName),
                internalName.substringAfterLast('/')
            )
        }

        fun fromType(type: Type): JavaClass {
            return fromInternalName(type.internalName)
        }
    }
}


fun typeFromImport(vararg parts: String): Type {
    return Type.getObjectType(parts.joinToString("/") { it.replace('.', '/') })
}

val MixinType = typeFromImport("org.spongepowered.asm.mixin.Mixin")

val OptionalType = typeFromImport("at.redi2go.photonics.mixin.Optional")
val ClientType = typeFromImport("at.redi2go.photonics.mixin.ClientSide")
val ServerType = typeFromImport("at.redi2go.photonics.mixin.ServerSide")

val Type.javaClass: JavaClass get() = JavaClass.fromType(this)

val Type.javaPackage: JavaPackage get() = JavaPackage.fromInternalName(this.internalName)
