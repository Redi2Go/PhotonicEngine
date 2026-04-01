plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(22)
}

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

    google()
    mavenCentral()

    gradlePluginPortal()
}

dependencies {
    implementation(plugin(sharedLibs.plugins.idea.gradle))
    implementation(plugin(sharedLibs.plugins.shadow))

    implementation(plugin(mcLibs.plugins.architectury.gradle))
    implementation(plugin(mcLibs.plugins.architectury.loom))
}

fun plugin(plugin: Provider<PluginDependency>): Provider<String> =
    plugin.map {
        val pluginId = it.pluginId
        val version = it.version

        "$pluginId:$pluginId.gradle.plugin:$version"
    }
