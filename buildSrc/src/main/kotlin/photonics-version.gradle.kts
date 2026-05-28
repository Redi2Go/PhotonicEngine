import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar

version = parent!!.version
group = parent!!.group

val photonics: PhotonicsExtension = project.extensions.create("photonics")

subprojects {
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

    version = parent!!.version
    group = parent!!.group

    val apiPath = ":modules:api"
    val corePath = ":modules:core"

    val commonPath = "${parent!!.path}:common"

    val jarName = { "photonics-${project.version}-${project.name}+MC-${photonics.minecraft.get()}" }

    val modulesPath = rootProject.childProjects["modules"]!!.projectDir
    val patchesPath = modulesPath.resolve("patches")
    val shadersPath = modulesPath.resolve("shaders")
    val resourcesPath = modulesPath.resolve("resources")

    beforeEvaluate {
        java {
            sourceCompatibility = photonics.javaVersion.orNull ?: JavaVersion.VERSION_17
            targetCompatibility = photonics.javaVersion.orNull ?: JavaVersion.VERSION_17
        }
    }

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
    }

    tasks {
        named<ProcessResources>("processResources") {
            if (project.name != "common") {
                from(patchesPath) {
                    into("/assets/photonics/patches/")
                }

                from(shadersPath) {
                    // This is under /shaders/photonics to make it easier to access with IPackPath
                    into("/assets/photonics/shaders")
                }

                from(resourcesPath) {
                    into("/")
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

        shadowJar {
            archiveFileName = provider { "${jarName()}-shaded.jar" }
            configurations = listOf(project.configurations.getByName("shadow"))


            from(impl.output)
        }
    }
}