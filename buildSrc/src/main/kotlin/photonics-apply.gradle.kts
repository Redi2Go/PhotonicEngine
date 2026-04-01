import dev.architectury.plugin.ArchitectPluginExtension
import org.gradle.kotlin.dsl.dependencies

plugins {
    id("architectury-plugin")
    id("dev.architectury.loom")
}

version = parent!!.version
group = parent!!.group

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}