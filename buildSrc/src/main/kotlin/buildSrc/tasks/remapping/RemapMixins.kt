package buildSrc.tasks.remapping

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.ClassRemapper
import java.nio.file.Path
import java.nio.file.StandardOpenOption.*
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolute
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.deleteRecursively
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes
import kotlin.math.max

private const val FILE_MODIFIED = 2
private const val FILE_REMOVED = 1

const val MIXIN_PACKAGE = "_mixins"

abstract class RemapMixins : DefaultTask() {
    @get:Input
    abstract val packagePrefix: Property<String>

    @get:Incremental
    @get:InputFiles
    protected abstract val inputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty
    @get:OutputDirectory
    abstract val classesOutputDir: DirectoryProperty

    init {
        classesOutputDir.value(outputDir.dir("classes"))
    }

    private val mixinPath: Path
        get() =
            classesOutputDir.getAsPath()
                .resolve(packagePrefix.get().replace('.', '/'))
                .resolve(MIXIN_PACKAGE)

    @TaskAction
    fun execute(inputs: InputChanges) {
        if (!inputs.isIncremental) clearOutputDir()

        val packagePrefix = packagePrefix.get()
        val changes = SourceChanges(inputs, inputDir)

        val mixinClasses = MixinClasses(mixinPath, packagePrefix)
        mixinClasses.remapAll(changes.modifiedFiles, packagePrefix)
        val remapper = mixinClasses.createRemapper()

        val classesOutputDir = classesOutputDir.getAsPath()
        changes.modifiedFiles.forEach {
            val reader = ClassReader(it.readBytes())

            val writer = ClassWriter(reader, 0)
            val classType = ClassTypeVisitor(Opcodes.ASM9, writer)
            val classRemapper = ClassRemapper(classType, remapper)
            reader.accept(classRemapper, 0)

            val classPackage = classType.type.packageName
            val className = classType.type.simpleName

            val file = classesOutputDir.resolve(classPackage.replace('.', '/'))
                .resolve("$className.class")
                .apply {
                    parent.createDirectories()
                    writeBytes(writer.toByteArray(), WRITE, CREATE, TRUNCATE_EXISTING)
                }
        }
    }

    @OptIn(ExperimentalPathApi::class)
    private fun clearOutputDir() {
        outputDir.getAsPath()
            .listDirectoryEntries()
            .map { it.deleteRecursively() }
    }

    companion object {
        fun TaskContainer.remapMixins(
            sourceName: String,
            configure: Action<RemapMixins> = Action { }
        ): TaskProvider<RemapMixins> {
            val taskMiddlePart = if (sourceName != "main") sourceName.replaceFirstChar(Char::uppercase) else ""

            return register<RemapMixins>("remap${taskMiddlePart}Mixins") {
                configure.execute(this)

                val compileJava = getByName<JavaCompile>("compile${taskMiddlePart}Java")
                inputDir.set(compileJava.destinationDirectory)

                dependsOn(compileJava)
            }
        }
    }
}

@JvmInline
value class SourceChanges private constructor(private val changes: Object2IntMap<Path>) {
    constructor(inputs: InputChanges, dir: DirectoryProperty) : this(Object2IntOpenHashMap()) {
        changes.defaultReturnValue(0)

        inputs.getFileChanges(dir)
            .asSequence()
            .map { it.changeType to it.file.toPath() }
            .filter { (_, path) -> path.isRegularFile() }
            .forEach { (changeType, path) ->
                changes.mergeInt(
                    path.absolute(),
                    if (changeType == ChangeType.REMOVED) FILE_REMOVED else FILE_MODIFIED,
                    ::max
                )
            }
    }

    val modifiedFiles: Sequence<Path> get() = getChanges(FILE_MODIFIED)
    val removedFiles: Sequence<Path> get() = getChanges(FILE_REMOVED)

    private fun getChanges(changeType: Int): Sequence<Path> =
        changes.object2IntEntrySet()
            .asSequence()
            .filter { it.intValue == changeType }
            .map { it.key }
}

private fun DirectoryProperty.getAsPath(): Path = get().asFile.toPath().toAbsolutePath()


