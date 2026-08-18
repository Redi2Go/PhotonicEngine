plugins {
    id("net.fabricmc.fabric-loom-remap")
    `photonics-fabric`
}

val mainLibs = libs12111

dependencies {
    // Required by sodium
    modRuntimeOnly(mainLibs.fabric.api)
}

tasks {
    processResources {
        inputs.property("version", project.version)

        val phVersion = constants.versions.photonics.get()
        val mcVersion = mainLibs.versions.minecraft.get()
        val fabricLoaderVersion = mainLibs.versions.fabric.loader.get()

        filesMatching("fabric.mod.json") {
            expand(
                "photonics_version" to phVersion,
                "minecraft_version" to mcVersion,
                "fabric_loader_version" to fabricLoaderVersion
            )
        }
    }
}
