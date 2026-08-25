import buildSrc.tasks.remapping.GenerateMixinFiles.Companion.generateMixinFiles
import buildSrc.tasks.remapping.MIXIN_PACKAGE
import buildSrc.tasks.remapping.REGISTRY_FILE_PATH
import buildSrc.tasks.remapping.RemapMixins.Companion.remapMixins
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar
import kotlin.io.path.isDirectory
import kotlin.io.path.relativeTo

version = parent!!.version
group = parent!!.group

val photonics: PhotonicsExtension = project.extensions.create("photonics")

subprojects {
    apply {
        plugin("com.gradleup.shadow")
        plugin("java")
    }

    java {
        withSourcesJar()
    }

    val main = sourceSets.getByName("main")
    val impl = sourceSets.create("impl") {
        compileClasspath += main.compileClasspath
        runtimeClasspath += main.runtimeClasspath
    }

    sourceSets {
        main {
            compileClasspath += impl.output
            runtimeClasspath += impl.output
        }
    }

    version = parent!!.version
    group = parent!!.group

    val gamePath = ":modules:game"
    val enginePath = ":modules:engine"

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
            add("shadow", add("implementation", project(commonPath, configuration = "namedElements")) {
                isTransitive = false
            }) {
                isTransitive = false
            }

            add("shadow", project(gamePath)) { isTransitive = false }
            add("shadow", project(enginePath)) { isTransitive = false }
        } else {
            add("implementation", project(gamePath))
            add("implementation", project(enginePath))
        }

        add("runtimeOnly", project(gamePath))
        add("runtimeOnly", project(enginePath))

        ext.set("proj", this@subprojects)
    }

    tasks {
        remapMixins(project.layout.buildDirectory) {
            packagePrefix = photonics.mixins.packageName

            if (project.name != "common") {
                inputMappings.add(
                    project(commonPath).layout
                        .buildDirectory
                        .file(REGISTRY_FILE_PATH)
                )
            }
        }

        if (project.name != "common") {
            generateMixinFiles {
                packageName = photonics.mixins.packageName.map { "$it.$MIXIN_PACKAGE" }
                compatabilityLevel = photonics.mixins.compatabilityLevel
                minVersion = photonics.mixins.minVersion

                outputDir = project.layout.buildDirectory.dir("generated/mixinFiles")
            }

            named<ProcessResources>("processResources") {
                val sourceSets = sourceSets.asSequence()
                    .map { it.name }
                    .toSet()

                val srcDir = layout.projectDirectory.dir("src")
                from(srcDir) {
                    into("/")

                    exclude {
                        val srcPath = srcDir.asFile.toPath()
                        val filePath = it.file.toPath()

                        if (filePath.isDirectory()) {
                            val relativePath = filePath.relativeTo(srcPath)
                            sourceSets.contains(relativePath.getName(0).toString())
                        } else false
                    }
                }

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

            addSources(project(gamePath))
            addSources(project(enginePath))
        }

        shadowJar {
            archiveFileName = "${jarName()}-shaded.jar"
            configurations = listOf(project.configurations.getByName("shadow"))


            from(impl.output)
        }
    }
}
