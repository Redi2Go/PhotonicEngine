import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

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

        architectury {
            if (project.name != "common") platformSetupLoomIde()

            if (project.name == "common") common("fabric")
            if (project.name == "fabric") fabric()

        }

        repositories {
            mavenCentral()
            mojang()
        }

        // Some of this is unnecessary and no I don't care
        dependencies {
            add("minecraft", "com.mojang:minecraft:${architecturyConfig.minecraft}")

            if (project.name != "common") {
                add(
                    "shadow",
                    add(
                        "implementation",
                        project(commonPath)
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

            shadowJar {
                from(impl.output)

                configurations = listOf(project.configurations.getByName("shadow"))
            }
        }
    }
}