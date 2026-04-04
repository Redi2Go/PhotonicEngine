import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar
import gradle.kotlin.dsl.accessors._6be35cdb3f46f0834efa6727df9adfd0.architectury
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask

plugins {
    id("architectury-plugin")
}

version = parent!!.version
group = parent!!.group

val architecturyConfig = architectury;

subprojects {
    apply(plugin = "architectury-plugin")
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "com.gradleup.shadow")

    apply(plugin = "java")

    java {
        withSourcesJar()
    }

    val main by sourceSets.getting
    val impl by sourceSets.creating {
        compileClasspath += main.compileClasspath
        runtimeClasspath += main.runtimeClasspath
    }

    sourceSets {
        main {
            compileClasspath += impl.output
            runtimeClasspath += impl.output
        }

        configureEach {
            java.srcDirs("$projectDir/src/${name}/mixins")
        }
    }

    beforeEvaluate {
        val dependencyBlock = architecturyConfig._dependencyBlock

        version = parent!!.version
        group = parent!!.group

        val apiPath = ":modules:api"
        val corePath = ":modules:core"

        val commonPath = "${parent!!.path}:common"

        val jarName = "photonics-${project.version}-${project.name}+MC-${architecturyConfig.minecraft}"

        java {
            sourceCompatibility = architecturyConfig.javaVersion ?: JavaVersion.VERSION_17
            targetCompatibility = architecturyConfig.javaVersion ?: JavaVersion.VERSION_17
        }

        architectury {
            if (project.name != "common") platformSetupLoomIde()

            if (project.name == "common") common("fabric")
            if (project.name == "fabric") fabric()

        }

        val modulesPath = rootProject.childProjects["modules"]!!.projectDir
        val patchesPath = modulesPath.resolve("patches")
        val shadersPath = modulesPath.resolve("shaders")

        repositories {
            mavenCentral()

            mojang()

            fabric()
            forge()
            architectury()
            modrinth()
        }

        // Some of this is unnecessary and no I don't care
        dependencies {
            add("minecraft", "com.mojang:minecraft:${architecturyConfig.minecraft}")

            if (project.name != "common") {
                add(
                    "shadow",
                    add(
                        "implementation",
                        project(commonPath, configuration = "namedElements")
                    ) {
                        isTransitive = false
                    }
                ) {
                    isTransitive = false
                }

                add("shadow", project(apiPath)) { isTransitive = false }
                add("shadow", project(corePath)) { isTransitive = false }
            } else {
                add("implementation", project(apiPath))
                add("implementation", project(corePath))
            }

            add("runtimeOnly", project(apiPath))
            add("runtimeOnly", project(corePath))

            ext.set("proj", this@subprojects)
            dependencyBlock?.execute(ArchitecturyCommonDependenciesScope(this))

            val fabricLoader = _fabricLoader

            // Fabric loader is needed on common for mixin dependency.
            // Why not just include the mixin dependency raw? I have no clue, ask architectury.
            if (fabricLoader != null && (project.name == "common" || project.name == "fabric")) {
                add("modImplementation", fabricLoader)
            }
        }

        tasks {
            named<ProcessResources>("processResources") {
                if (project.name != "common") {
                    from(patchesPath) {
                        into("/assets/photonics/patches/")
                    }

                    from(shadersPath) {
                        // This is under /shaders/photonics to make it easier to access with IPackPath
                        into("/assets/photonics/shaders/photonics/")
                    }
                }
            }

            named<Jar>("jar") {
                from(impl.output)
            }

            named<Jar>("sourcesJar") {
                fun addSources(sourceSet: SourceSet) {
                    from(sourceSet.java.srcDirs)
                    from(sourceSet.resources.srcDirs)
                }

                fun addSources(project: Project) {
                    addSources(project.sourceSets["main"])
                    addSources(project.sourceSets.findByName("impl") ?: return)
                }


                addSources(impl)

                if (project.name != "common")
                    addSources(project(commonPath))

                addSources(project(apiPath))
                addSources(project(corePath))
            }

            named<RemapJarTask>("remapJar") {
                archiveFileName = "$jarName-unshaded.jar"
            }

            named<RemapSourcesJarTask>("remapSourcesJar") {
                archiveFileName = "$jarName-sources.jar"
            }

            shadowJar {
                archiveFileName = "$jarName.jar"
                configurations = listOf(project.configurations.getByName("shadow"))


                from(impl.output)
            }
        }
    }
}