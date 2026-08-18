package buildSrc.tasks.remapping

import org.objectweb.asm.Type

fun typeFromImport(vararg parts: String): Type {
    return Type.getObjectType(parts.joinToString("/") { it.replace('.', '/') })
}

val MixinType = typeFromImport("org.spongepowered.asm.mixin.Mixin")

val Type.simpleName: String
    get() = internalName.substringAfterLast('/')

val Type.packageName: String
    get() = internalName.substringBeforeLast('/').replace('/', '.')
