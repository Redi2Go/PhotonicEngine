package buildSrc.tasks.remapping

import buildSrc.tasks.remapping.parsing.JavaPackage
import buildSrc.tasks.remapping.parsing.ClassRegistry
import buildSrc.tasks.remapping.parsing.JavaClass
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper
import java.nio.file.DirectoryNotEmptyException
import java.nio.file.Path
import java.nio.file.StandardOpenOption.*
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.PathWalkOption
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteExisting
import kotlin.io.path.deleteRecursively
import kotlin.io.path.div
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readBytes
import kotlin.io.path.relativeTo
import kotlin.io.path.walk
import kotlin.io.path.writeBytes

const val MIXIN_PACKAGE = "_mixins"

abstract class RemapMixins : DefaultTask() {
    @get:Input
    abstract val packagePrefix: Property<String>

//    @get:InputFiles
//    protected abstract val inputMappings: FileCollection

    @get:Incremental
    @get:InputDirectory
    protected abstract val inputDir: DirectoryProperty

    @get:OutputDirectory
    protected abstract val outputDir: DirectoryProperty

    @TaskAction
    fun execute(changes: InputChanges) {
        val inputDir = inputDir.getAsPath()
        val outputDir = outputDir.getAsPath()

        val packagePrefix = JavaPackage.fromImport(packagePrefix.get())
        val registry = scanClasses(inputDir, packagePrefix)

        removeStaleFiles(changes, registry)

        val remapper = registry.createRemapper()
        changes.getFileChanges(this.inputDir)
            .asSequence()
            .filter { it.changeType != ChangeType.REMOVED }
            .map { it.file.toPath() }
            .distinct()
            .forEach {
                val relatvieDir = it.relativeTo(inputDir)
                val srcName = relatvieDir.getName(0).toString()

                val javaClass = registry.remap(JavaClass.fromPath(it, inputDir / srcName))
                val outputFile = javaClass.toPath(outputDir / srcName)

                outputFile.parent.createDirectories()
                outputFile.writeBytes(remapClass(it, remapper), WRITE, CREATE, TRUNCATE_EXISTING)
            }
    }

    private fun scanClasses(dir: Path, packagePrefix: JavaPackage): ClassRegistry {
        val registry = ClassRegistry()

        registry.addAll(
            dir.listDirectoryEntries().asSequence().flatMap { it.walk() },
            packagePrefix
        )

        return registry
    }

    private fun removeStaleFiles(changes: InputChanges, registry: ClassRegistry) {
        when {
            changes.isIncremental -> clearOutputDir()

            else -> for (sourceSet in outputDir.getAsPath().listDirectoryEntries()) {
                sourceSet.walk(PathWalkOption.INCLUDE_DIRECTORIES)
                    .toList()
                    .asReversed()
                    .filter { !registry.contains(JavaClass.fromPath(it, sourceSet)) || it.isDirectory() }
                    .forEach { it.deleteIfEmpty() }
            }
        }
    }

    private fun remapClass(file: Path, remapper: Remapper): ByteArray {
        val reader = ClassReader(file.readBytes())

        val writer = ClassWriter(reader, 0)
        val classRemapper = ClassRemapper(writer, remapper)
        reader.accept(classRemapper, 0)

        return writer.toByteArray()
    }

    @OptIn(ExperimentalPathApi::class)
    private fun clearOutputDir() {
        outputDir.getAsPath()
            .listDirectoryEntries()
            .map { it.deleteRecursively() }
    }

    companion object {
        fun TaskContainer.remapMixins(
            buildDir: DirectoryProperty,
            configure: Action<RemapMixins> = Action { }
        ): TaskProvider<RemapMixins> {
            val compileOutputDir = buildDir.dir("compilation/classes/java")
            val remapOutputDir = buildDir.dir("classes/java")

            val javaCompiles = withType<JavaCompile>()
                .named { !it.contains("test", ignoreCase = true) }

            javaCompiles.configureEach {
                val name = destinationDirectory.getAsPath().name

                destinationDirectory = compileOutputDir.map { it.dir(name) }
            }

            val remapMixins = register<RemapMixins>("remapMixins") {
                dependsOn(javaCompiles)

                inputDir = compileOutputDir
                outputDir = remapOutputDir

                configure.execute(this)
            }

            named("classes") {
                dependsOn(remapMixins)
            }

            return remapMixins
        }
    }
}

private fun DirectoryProperty.getAsPath(): Path = get().asFile.toPath().toAbsolutePath()

private fun Path.deleteIfEmpty(): Boolean {
    try {
        deleteExisting()
        return true
    } catch (ex: DirectoryNotEmptyException) {
        return false;
    }
}
