package buildSrc.tasks.remapping

import buildSrc.tasks.remapping.parsing.ClassRegistry
import buildSrc.tasks.remapping.parsing.JavaPackage
import buildSrc.tasks.remapping.parsing.MixinJson
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.JavaVersion
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources
import kotlin.io.path.createDirectories
import kotlin.io.path.name
import kotlin.text.contains

const val GENERATE_MIXIN_FILES_TASK = "generateMixinFiles"

abstract class GenerateMixinFiles : DefaultTask() {
    @get:Input abstract val packageName: Property<String>
    @get:Input abstract val compatabilityLevel: Property<JavaVersion>
    @get:Input abstract val minVersion: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:InputFiles
    @get:IgnoreEmptyDirectories
    protected abstract val inputRegistry: RegularFileProperty

    @get:OutputFile protected abstract val requiredMixinsFile: RegularFileProperty
    @get:OutputFile protected abstract val optionalMixinsFile: RegularFileProperty

    init {
        requiredMixinsFile = outputDir.file("photonics-required.mixins.json")
        optionalMixinsFile = outputDir.file("photonics-optional.mixins.json")
    }

    @TaskAction
    fun execute() {
        val requiredMixinFile = requiredMixinsFile.getAsPath()
        val optionalMixinFile = optionalMixinsFile.getAsPath()

        val registry = ClassRegistry()
        registry.readFrom(inputRegistry.getAsPath())

        val mixinPackage = JavaPackage.fromImport(packageName.get())

        val requiredMixins = MixinJson(true, packageName.get(), compatabilityLevel.get(), minVersion.get())
        val optionalMixins = MixinJson(false, packageName.get(), compatabilityLevel.get(), minVersion.get())

        for (mixin in registry.mixins) {
            val jsonFile = if (!mixin.isOptional) requiredMixins else optionalMixins
            val mixinName = mixin.type.getMixinName(mixinPackage)

            jsonFile.addMixin(mixin.env, mixinName)
        }

        requiredMixins.writeTo(requiredMixinFile)
        optionalMixins.writeTo(optionalMixinFile)
    }

    companion object {
        fun TaskContainer.generateMixinFiles(configure: Action<GenerateMixinFiles> = Action { }): TaskProvider<GenerateMixinFiles> {
            val remapMixins = named<RemapMixins>(REMAP_MIXINS_TASK)

            val generateMixins = register<GenerateMixinFiles>(GENERATE_MIXIN_FILES_TASK) {
                dependsOn(remapMixins)

                inputRegistry = remapMixins.flatMap { it.registryOutput }

                configure.execute(this)
            }

            named<ProcessResources>("processResources") {
                dependsOn(generateMixins)

                from(generateMixins.flatMap { it.outputDir }) {
                    into("/")
                }
            }

            return generateMixins
        }
    }
}
