val phConfig = parent!!.extensions.getByName<PhotonicsExtension>("photonics")

dependencies {
    add("minecraft", "com.mojang:minecraft:${phConfig.minecraft.get()}")
    phConfig._dependencyBlock.orNull?.execute(PhotonicsCommonDependenciesScope(this))

    val fabricLoader = _fabricLoader

    // Fabric loader is needed on common for mixin dependency.
    // Why not just include the mixin dependency raw? I have no clue, ask architectury.
    if (fabricLoader != null) {
        add("modImplementation", fabricLoader)
    }
}