import kotlin.text.replace

rootProject.name = "photonics"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net") {
            name = "Fabric"
        }

        maven("https://maven.minecraftforge.net/") {
            name = "Forge"
        }

        maven("https://maven.architectury.dev/") {
            name = "Architectury"
        }

        maven("https://api.modrinth.com/maven") {
            name = "Modrinth"

            content {
                includeGroup("maven.modrinth")
            }
        }

        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalog(name = "constants", "modules/constants.toml")
    versionCatalog(name = "sharedLibs", "modules/shared_libs.toml")

    versionCatalog(name = "coreLibs", "modules/core/core_libs.toml")

    versionCatalog(name = "mcLibs", "modules/versions/mc_libs.toml")
}

include(":modules:api")
include(":modules:core")
include(":modules:versions")

includeMcVersion("1.21.11")
















// out of sight out of mind


fun DependencyResolutionManagement.versionCatalog(name: String, path: String) {
    versionCatalogs {
        create(name) {
            from(files(path))
        }
    }
}

fun cleanupMcVersion(version: String): Pair<String, String> {
    val underscoreVersion = version.replace(".", "_")
    val cleanVersion = underscoreVersion.replace("_", "")

    return underscoreVersion to cleanVersion
}

fun includeMcVersion(version: String) {
    val (underscoreVersion, cleanVersion) = cleanupMcVersion(version)
    val projectPath = ":modules:versions:mc${cleanVersion}"

    include(projectPath)
    val project = project(projectPath)
    project.projectDir = file("modules/versions/$underscoreVersion")

    val sources = arrayOf("common", "fabric")
    for (source in sources) {
        val sourcePath = "$projectPath:$source"

        include(sourcePath)
        val sourceProject = project(sourcePath)
        sourceProject.projectDir = file("modules/versions/$underscoreVersion/$source")
    }

    dependencyResolutionManagement {
        versionCatalog(name = "libs$cleanVersion", "modules/versions/${underscoreVersion}/main_libs.toml")
    }
}