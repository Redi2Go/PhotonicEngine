plugins {
    id("net.fabricmc.fabric-loom-remap")
    `photonics-common`
}

dependencies {
    add("mappings", loom.officialMojangMappings())
}