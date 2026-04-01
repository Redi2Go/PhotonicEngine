rootProject.name = "buildSrc"


dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    // Reuse the version catalog from the main build.
    versionCatalog("sharedLibs", "../modules/shared_libs.toml")
    versionCatalog("mcLibs", "../modules/versions/mc_libs.toml")
}

















fun DependencyResolutionManagement.versionCatalog(name: String, path: String) {
    versionCatalogs {
        create(name) {
            from(files(path))
        }
    }
}