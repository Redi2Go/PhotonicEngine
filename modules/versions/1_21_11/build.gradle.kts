import org.gradle.kotlin.dsl.accessors.runtime.addDependencyTo

val mainLibs = libs12111

architectury {
    minecraft = mainLibs.versions.minecraft.get()

    dependencies {
        mappings(loom.officialMojangMappings())
        shadow(sharedLibs.semver)
    }
}