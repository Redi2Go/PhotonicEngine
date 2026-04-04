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

fun RepositoryHandler.forge() {
    maven("https://maven.minecraftforge.net/") {
        name = "Forge"
    }
}

fun RepositoryHandler.architectury() {
    maven("https://maven.architectury.dev/") {
        name = "Architectury"
    }
}

fun RepositoryHandler.modrinth() {
    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"

        content {
            includeGroup("maven.modrinth")
        }
    }
}