package buildSrc.tasks.remapping.parsing

data class Mixin(
    val type: JavaClass,
    val env: MixinEnv,
    val isOptional: Boolean
)
