val phConfig = parent!!.extensions.getByName<PhotonicsExtension>("photonics")

dependencies {
    add("minecraft", "com.mojang:minecraft:1.21.11")
    phConfig._dependencyBlock.orNull?.execute(PhotonicsCommonDependenciesScope(this))
}