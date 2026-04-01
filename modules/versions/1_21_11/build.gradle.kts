val mainLibs = libs12111

architectury {
    minecraft = mainLibs.versions.minecraft.version.get()

    dependencies {
        mappings(loom.officialMojangMappings())

        include(implementation(projects.modules.api))
        include(implementation(projects.modules.core))
    }
}