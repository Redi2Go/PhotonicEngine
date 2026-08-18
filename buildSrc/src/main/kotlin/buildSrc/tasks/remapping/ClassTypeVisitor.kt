package buildSrc.tasks.remapping

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Type

open class ClassTypeVisitor(api: Int, classVisitor: ClassVisitor?) : ClassVisitor(api, classVisitor) {
    lateinit var type: Type

    constructor(api: Int) : this(api, null)

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        type = Type.getObjectType(name)
        super.visit(version, access, name, signature, superName, interfaces)
    }
}
