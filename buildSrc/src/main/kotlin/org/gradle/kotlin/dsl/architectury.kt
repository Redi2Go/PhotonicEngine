package org.gradle.kotlin.dsl

import dev.architectury.plugin.ArchitectPluginExtension
import gradle.kotlin.dsl.accessors._6be35cdb3f46f0834efa6727df9adfd0.ext
import gradle.kotlin.dsl.accessors._7048eba8028ec3d3412229f1a745078f.api
import gradle.kotlin.dsl.accessors._7048eba8028ec3d3412229f1a745078f.compileOnly
import gradle.kotlin.dsl.accessors._7048eba8028ec3d3412229f1a745078f.implementation
import gradle.kotlin.dsl.accessors._7048eba8028ec3d3412229f1a745078f.include
import gradle.kotlin.dsl.accessors._7048eba8028ec3d3412229f1a745078f.loom
import gradle.kotlin.dsl.accessors._7048eba8028ec3d3412229f1a745078f.mappings
import gradle.kotlin.dsl.accessors._7048eba8028ec3d3412229f1a745078f.modApi
import gradle.kotlin.dsl.accessors._7048eba8028ec3d3412229f1a745078f.modCompileOnly
import gradle.kotlin.dsl.accessors._7048eba8028ec3d3412229f1a745078f.modImplementation
import gradle.kotlin.dsl.accessors._7048eba8028ec3d3412229f1a745078f.modRuntimeOnly
import gradle.kotlin.dsl.accessors._7048eba8028ec3d3412229f1a745078f.runtimeOnly
import gradle.kotlin.dsl.accessors._7048eba8028ec3d3412229f1a745078f.testCompileOnly
import gradle.kotlin.dsl.accessors._7048eba8028ec3d3412229f1a745078f.testImplementation
import gradle.kotlin.dsl.accessors._7048eba8028ec3d3412229f1a745078f.testRuntimeOnly
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Action
import org.gradle.api.Project

@JvmInline
value class ArchitecturyCommonDependenciesScope(
    private val backing: DependencyHandlerScope
) {
    val project: Project
        get() = backing.ext.get("proj") as Project

    val loom: LoomGradleExtensionAPI
        get() = project.loom

    // workaround for these being internal in version build file
    fun mappings(dependencyNotation: Any) = backing.mappings(dependencyNotation)!!

    fun api(dependencyNotation: Any) = backing.api(dependencyNotation)!!
    fun compileOnly(dependencyNotation: Any) = backing.compileOnly(dependencyNotation)!!
    fun implementation(dependencyNotation: Any) = backing.implementation(dependencyNotation)!!
    fun include(dependencyNotation: Any) = backing.include(dependencyNotation)!!
    fun runtimeOnly(dependencyNotation: Any) = backing.runtimeOnly(dependencyNotation)!!

    fun modApi(dependencyNotation: Any) = backing.modApi(dependencyNotation)!!
    fun modCompileOnly(dependencyNotation: Any) = backing.modCompileOnly(dependencyNotation)!!
    fun modImplementation(dependencyNotation: Any) = backing.modImplementation(dependencyNotation)!!
    fun modRuntimeOnly(dependencyNotation: Any) = backing.modRuntimeOnly(dependencyNotation)!!

    fun testCompileOnly(dependencyNotation: Any) = backing.testCompileOnly(dependencyNotation)!!
    fun testImplementation(dependencyNotation: Any) = backing.testImplementation(dependencyNotation)!!
    fun testRuntimeOnly(dependencyNotation: Any) = backing.testRuntimeOnly(dependencyNotation)!!
}

fun ArchitectPluginExtension.dependencies(action: Action<ArchitecturyCommonDependenciesScope>) {
    ext.set("dependencies", action)
}