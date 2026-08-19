package buildSrc.tasks.remapping

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Type

class MixinDataVisitor(api: Int, classVisitor: ClassVisitor?) : ClassTypeVisitor(api, classVisitor) {
    var isMixin = false
    var isRequired = true
    var env = MixinEnv.COMMON

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
        when(val type = Type.getType(descriptor)) {
            MixinType -> isMixin = true

            OptionalType -> isRequired = false

            ClientType -> {
                check(env == MixinEnv.COMMON) { "${type.simpleName} is already marked for ${env.name}" }
                env = MixinEnv.CLIENT
            }

            ServerType -> {
                check(env == MixinEnv.COMMON) { "${type.simpleName} is already marked for ${env.name}" }
                env = MixinEnv.SERVER
            }
        }

        return super.visitAnnotation(descriptor, visible)
    }
}
