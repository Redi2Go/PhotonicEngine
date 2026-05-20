plugins {
    id(mcLibs.plugins.loom.gradle.get().pluginId) apply false
    id(mcLibs.plugins.loom.remap.get().pluginId) apply false

    `photonics-version` apply false
    `photonics-common` apply false
    `photonics-fabric` apply false
}

version = constants.versions.photonics.get()
group = constants.versions.mavenGroup.get()

subprojects {
    apply(plugin = "photonics-version")
}