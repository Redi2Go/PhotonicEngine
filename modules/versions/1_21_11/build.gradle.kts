val mainLibs = libs12111

architectury {
    minecraft = mainLibs.versions.minecraft.get()

    commonDependencies {
        mappings(loom.officialMojangMappings())
        shadow(sharedLibs.semver)
    }
}