package org.gradle.kotlin.dsl

import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

inline val Project.loom: LoomGradleExtensionAPI
    get() = extensions.getByName<LoomGradleExtensionAPI>("loom")

fun Project.loom(block: Action<LoomGradleExtensionAPI>) {
    extensions.configure("loom", block)
}

inline val Project.java: JavaPluginExtension
    get() = extensions.getByName<JavaPluginExtension>("java")

fun Project.java(configure: Action<JavaPluginExtension>) {
    extensions.configure("java", configure)
}

inline val Project.sourceSets: SourceSetContainer
    get() = extensions.getByName<SourceSetContainer>("soureSets")

fun Project.sourceSets(configure: Action<SourceSetContainer>) {
    extensions.configure("sourceSets", configure)
}

val SourceSetContainer.`main`: NamedDomainObjectProvider<SourceSet>
    get() = named<SourceSet>("main")