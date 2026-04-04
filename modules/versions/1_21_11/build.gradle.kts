val mainLibs = libs12111

architectury {
    minecraft = mainLibs.versions.minecraft.get()
    javaVersion = JavaVersion.VERSION_21

    commonDependencies {
        mappings(loom.officialMojangMappings())

        // Use by fabric (for obvious reasons) & common for mixin dependencies
        fabricLoader(mainLibs.fabric.loader)

        shadow(sharedLibs.semver)
    }
}