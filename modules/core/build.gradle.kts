plugins {
    id("java")
}

version = constants.versions.photonics
group = constants.versions.mavenGroup

base.archivesName = "photonics-core"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    mojang()
}

dependencies {
    implementation(sharedLibs.joml)
    implementation(sharedLibs.gson)
    implementation(sharedLibs.semver)
    implementation(sharedLibs.dataFixerUpper)
    implementation(sharedLibs.brigadier)

    implementation(projects.modules.api)
    implementation(coreLibs.jetrains.annotations)
    implementation(coreLibs.slf4j.api)
}


tasks {
    jar {
        inputs.property("archivesName", project.base.archivesName)
    }
}