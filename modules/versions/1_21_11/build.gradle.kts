val mainLibs = libs12111

photonics {
    minecraft = mainLibs.versions.minecraft.get()
    javaVersion = JavaVersion.VERSION_21

    commonDependencies {
        mappings(loom.officialMojangMappings())

         //Use by fabric (for obvious reasons) & common for mixin dependencies
        fabricLoader(mainLibs.fabric.loader)

        shadow(sharedLibs.semver)
        shadow(sharedLibs.fastutil.concurrent.wrapper) {
            isTransitive = false
        }

        runtimeOnly(mainLibs.antlr4.runtime)
        implementation(mainLibs.glsl.transformer)
        implementation(mainLibs.jcpp)

        modImplementation(mainLibs.sodium)
        modImplementation(mainLibs.iris)
    }
}