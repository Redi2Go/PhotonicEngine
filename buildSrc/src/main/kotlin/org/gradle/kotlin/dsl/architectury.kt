package org.gradle.kotlin.dsl

import dev.architectury.plugin.ArchitectPluginExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.accessors.runtime.addConfiguredDependencyTo
import org.gradle.kotlin.dsl.accessors.runtime.addDependencyTo
import org.gradle.language.jvm.tasks.ProcessResources

@JvmInline
value class ArchitecturyCommonDependenciesScope(
    private val backing: DependencyHandlerScope
) {
    val project: Project
        get() = backing.ext.get("proj") as Project

    val loom: LoomGradleExtensionAPI
        get() = project.loom

    private fun add(configuration: String, dependencyNotation: Any?, action: Action<ExternalModuleDependency>) {
        if (dependencyNotation is Provider<*>) {
            addConfiguredDependencyTo(backing.dependencies, configuration, dependencyNotation, action)
        } else {
            addDependencyTo(backing.dependencies, configuration, dependencyNotation!!, action)
        }
    }

    // workaround for these being internal in version build file
    fun mappings(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("mappings", dependencyNotation, configureAction)

    fun shadow(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("shadow", dependencyNotation!!, configureAction)


    fun api(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("api", dependencyNotation!!, configureAction)
    fun compileOnly(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("compileOnly", dependencyNotation!!, configureAction)
    fun implementation(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("implementation", dependencyNotation!!, configureAction)
    fun include(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("include", dependencyNotation!!, configureAction)
    fun runtimeOnly(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("runtimeOnly", dependencyNotation!!, configureAction)


    fun modApi(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("modApi", dependencyNotation!!, configureAction)
    fun modCompileOnly(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("modCompileOnly", dependencyNotation!!, configureAction)
    fun modImplementation(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("modImplementation", dependencyNotation!!, configureAction)
    fun modRuntimeOnly(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("modRuntimeOnly", dependencyNotation!!, configureAction)


    fun testCompileOnly(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("testCompileOnly", dependencyNotation!!, configureAction)
    fun testImplementation(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("testImplementation", dependencyNotation!!, configureAction)
    fun testRuntimeOnly(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("testRuntimeOnly", dependencyNotation!!, configureAction)
}

var ArchitectPluginExtension._dependencyBlock: Action<ArchitecturyCommonDependenciesScope>? by Extensions
var ArchitectPluginExtension._resourceBlock: Action<ProcessResources>? by Extensions

fun ArchitectPluginExtension.commonDependencies(action: Action<ArchitecturyCommonDependenciesScope>) {
    _dependencyBlock = action
}