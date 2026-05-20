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

        filesMatching("fabric.mod.json") {
            expand(
                "photonics_version" to constants.versions.photonics.get(),
                "minecraft_version" to mainLibs.versions.minecraft.get(),
                "fabric_loader_version" to mainLibs.versions.fabric.loader.get()
            )
        }
    }
}