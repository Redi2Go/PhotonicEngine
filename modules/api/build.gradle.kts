plugins {
    id("java")
}

version = constants.versions.photonics.get()
group = constants.versions.mavenGroup.get()

base.archivesName = "photonics-mc-api"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    withSourcesJar()
}

repositories {
    mavenCentral()
    mojang()
}

dependencies {
    implementation(sharedLibs.joml)
    implementation(sharedLibs.dataFixerUpper)
    implementation(sharedLibs.brigadier)
}


tasks {
    jar {
        inputs.property("archivesName", project.base.archivesName)
    }
}