import gradle.kotlin.dsl.accessors._7048eba8028ec3d3412229f1a745078f.minecraft

plugins {
    id("architectury-plugin")
}

version = parent!!.version
group = parent!!.group

val architecturyConfig = architectury;

subprojects {
    apply(plugin = "photonics-apply")

    beforeEvaluate {
        dependencies {
            @Suppress("UNCHECKED_CAST")
            val commonDependencies = architecturyConfig.ext.get("dependencies") as Action<ArchitecturyCommonDependenciesScope>

            minecraft("com.mojang:minecraft:${architecturyConfig.minecraft}")

            ext.set("proj", this@subprojects)
            commonDependencies.execute(ArchitecturyCommonDependenciesScope(this))
        }
    }
}