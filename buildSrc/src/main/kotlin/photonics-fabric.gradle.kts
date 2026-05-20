import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask

val phConfig = parent!!.extensions.getByName<PhotonicsExtension>("photonics")

dependencies {
    add("minecraft", "com.mojang:minecraft:1.21.11")
    phConfig._dependencyBlock.orNull?.execute(PhotonicsCommonDependenciesScope(this))
}

val jarName = "photonics-${project.version}-${project.name}+MC-${phConfig.minecraft}"

tasks {
    named<RemapJarTask>("remapJar") {
        archiveFileName = "$jarName.jar"

        dependsOn(shadowJar)
        inputFile = shadowJar.get().archiveFile;
    }

    named<RemapSourcesJarTask>("remapSourcesJar") {
        archiveFileName = "$jarName-sources.jar"
    }
}