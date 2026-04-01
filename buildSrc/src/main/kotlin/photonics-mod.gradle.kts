import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar
import org.gradle.internal.fingerprint.classpath.impl.ClasspathFingerprintingStrategy.compileClasspath
import org.gradle.internal.fingerprint.classpath.impl.ClasspathFingerprintingStrategy.runtimeClasspath
import org.gradle.kotlin.dsl.invoke

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
    }

    sourceSets {
        val impl by creating

        main {
            compileClasspath += impl.output
            runtimeClasspath += impl.output

            resources.srcDirs(impl.resources.srcDirs)
        }

        configureEach {
            java.srcDirs("$projectDir/src/${name}/mixins")
        }
    }

    beforeEvaluate {
        val dependencyBlock = architecturyConfig._dependencyBlock

        version = parent!!.version
        group = parent!!.group

        architectury {
            if (project.name != "common") platformSetupLoomIde()

            if (project.name == "common") common("fabric")
            if (project.name == "fabric") fabric()

        }

        repositories {
            mavenCentral()
            mojang()
        }

        // Some of this is unnesscary and no I don't care
        dependencies {
            @Suppress("UNCHECKED_CAST")
            add("minecraft", "com.mojang:minecraft:${architecturyConfig.minecraft}")

            if (project.name != "common") {
                add(
                    "shadow",
                    add(
                        "implementation",
                        project("${parent!!.path}:common")
                    ) {
                        isTransitive = false
                    }
                ) {
                    isTransitive = false
                }

                add("shadow", project(":modules:api")) { isTransitive = false }
                add("shadow", project(":modules:core")) { isTransitive = false }
            } else {
                add("implementation", project(":modules:api"))
                add("implementation", project(":modules:core"))
            }

            ext.set("proj", this@subprojects)
            dependencyBlock?.execute(ArchitecturyCommonDependenciesScope(this))
        }

        tasks {
            shadowJar {
                configurations = listOf(project.configurations.getByName("shadow"))
            }
        }
    }
}