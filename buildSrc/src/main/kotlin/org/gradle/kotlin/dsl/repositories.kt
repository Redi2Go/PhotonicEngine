package org.gradle.kotlin.dsl

import org.gradle.api.artifacts.dsl.RepositoryHandler

fun RepositoryHandler.mojang() {
    maven("https://libraries.minecraft.net") {
        name = "Mojang"
    }
}

fun RepositoryHandler.fabric() {
    maven("https://maven.fabricmc.net") {
        name = "Fabric"
    }
}