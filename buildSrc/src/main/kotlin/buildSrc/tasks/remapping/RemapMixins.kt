package buildSrc.tasks.remapping

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.gradle.api.JavaVersion
import org.gradle.api.tasks.InputFiles
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
import kotlin.io.path.deleteRecursively
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readBytes
import kotlin.io.path.walk
import kotlin.io.path.writeBytes
import kotlin.math.max

private const val FILE_MODIFIED = 2
private const val FILE_REMOVED = 1

const val MIXIN_PACKAGE = "_mixins"

abstract class RemapMixins : DefaultTask() {
    @get:Input abstract val packagePrefix: Property<String>
    @get:Input abstract val compatibilityLevel: Property<JavaVersion>
    @get:Input abstract val minVersion: Property<String>
    @get:Input abstract val mixinPrefix: Property<String>

    @get:InputFiles
    protected abstract val inputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val classesOutputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val resourcesOutputDir: DirectoryProperty

    init {
        classesOutputDir.value(outputDir.dir("classes"))
        resourcesOutputDir.value(outputDir.dir("resources"))
    }

    private val mixinPath: Path
        get() = classesOutputDir.getAsPath()
            .resolve(packagePrefix.get().replace('.', '/'))
            .resolve(MIXIN_PACKAGE)

    private val inputFiles: Sequence<Path>
        get() = inputDir.getAsPath().walk()

    @TaskAction
    fun execute() {
        clearOutputDir()

        val packagePrefix = packagePrefix.get()

        val mixinClasses = MixinClasses()
        mixinClasses.remapAll(inputFiles, packagePrefix)

        val remapper = mixinClasses.createRemapper()
        val classesOutputDir = classesOutputDir.getAsPath()

        val mixinPackage = "$packagePrefix.$MIXIN_PACKAGE"
        val requiredMixins = MixinJson(true, mixinPackage, compatibilityLevel.get(), minVersion.get())
        val optionalMixins = requiredMixins.optional()

        inputFiles.forEach {
            val reader = ClassReader(it.readBytes())

            val writer = ClassWriter(reader, 0)
            val mixinData = MixinDataVisitor(Opcodes.ASM9, writer)
            val classRemapper = ClassRemapper(mixinData, remapper)
            reader.accept(classRemapper, 0)

            val classPackage = mixinData.type.packageName
            val className = mixinData.type.simpleName

            if (mixinData.isMixin) {
                val mixinFile = if (mixinData.isRequired) requiredMixins else optionalMixins

                val mixinBuilder = StringBuilder()
                mixinBuilder.append(classPackage.removePrefix(mixinPackage))
                mixinBuilder.append('.')
                mixinBuilder.append(className)

                mixinFile.addMixin(
                    mixinData.env,
                    mixinBuilder.toString().trim { it == '.' }
                )
            }

            val file = classesOutputDir.resolve(classPackage.replace('.', '/'))
                .resolve("$className.class")
                .apply {
                    parent.createDirectories()
                    writeBytes(writer.toByteArray(), WRITE, CREATE, TRUNCATE_EXISTING)
                }
        }

        if (!requiredMixins.isEmpty())
            requiredMixins.writeTo(resourcesOutputDir.getAsPath().resolve("${mixinPrefix.get()}.mixins.json"))

        if (!optionalMixins.isEmpty())
            optionalMixins.writeTo(resourcesOutputDir.getAsPath().resolve("${mixinPrefix.get()}-optional.mixins.json"))
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

private fun DirectoryProperty.getAsPath(): Path = get().asFile.toPath().toAbsolutePath()


