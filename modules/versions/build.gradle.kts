plugins {
    id(mcLibs.plugins.architectury.gradle.get().pluginId) apply false
    id(mcLibs.plugins.architectury.loom.get().pluginId) apply false

    `photonics-mod` apply false
}

version = constants.versions.photonics
group = constants.versions.mavenGroup

subprojects {
    apply(plugin = "photonics-mod")
}